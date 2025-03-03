package com.pay.ioopos.support.serialport.internal;

import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.HexUtils.toHexString;

import android.util.Log;

import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.common.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

/**
 * @author mo_yq5
 * @since 2021-06-24
 */
public abstract class SerialPortFactory {

    private static final String TAG = SerialPortFactory.class.getSimpleName();

    private static final boolean KEEP_OPENING = true;

    private static final ConcurrentMap<String, SerialPort> map = new ConcurrentHashMap<>();

    static {
        addSerialPort("/dev/ttyUSB0", 9600);// 外接串口
        addSerialPort("/dev/ttyMT1", 9600);// SP306/SP308 NFC模块
        addSerialPort("/dev/ttyMT1", 115200);// SP306+/SP308+ 扫码、NFC模块
        addSerialPort("/dev/ttyMT2", 115200);// SP306PRO 扫码、NFC模块
    }
    public static boolean isKeepOpen() {
        return KEEP_OPENING;
    }
    public static SerialPort getSerialPort(String path, int baudrate) {
        synchronized (map) {
            return map.get(path + baudrate);
        }
    }

    public static void addSerialPort(String path, int baudrate) {
        synchronized (map) {
            map.put(path + baudrate, new SerialPort(path, baudrate, KEEP_OPENING));
        }
    }

    public static void release() {// 可通过ThreadPoolUtils.release(); 统一释放
        synchronized (map) {
            map.forEach((key, serial) -> {
                serial.stop();
            });
        }
    }

    public static class SerialPort {
        private final Object lock = new Object();
        private final File file;
        private final int baudrate;
        private CmdCaseAbstract cmdCase;// 当前业务（指令集）
        private boolean isRunningNormally = false;
        private boolean isNewCase = true;// 是否为新业务（指令集）
        private boolean isBizing = false;// 是否执行业务
        private boolean isKeepOpening = false;// 是否串口长开，否则完成一次业务指令（集）后关闭，下次业务重新打开
        private Future<?> sendingFuture;
        private Future<?> readingFuture;
        private Future<?> serialFuture;
        private volatile boolean isSended = false;
        public SerialPort(String path, int baudrate) {
            this.file = new File(path);
            this.baudrate = baudrate;
        }

        public SerialPort(String path, int baudrate, boolean isKeepOpening) {
            this.file = new File(path);
            this.baudrate = baudrate;
            this.isKeepOpening = isKeepOpening;
        }

        public void start(CmdCaseAbstract cmdCase) {
            synchronized (lock) {
                this.isNewCase = true;
                this.cmdCase = cmdCase;
                this.isBizing = true;
                this.isSended = false;

                if (isKeepOpening) {
                    if (!isRunningNormally) {
                        startSerialPort();
                    }
                    return;
                }
                startSerialPort();
            }

        }

        public void stop() {
            synchronized (lock) {
                isBizing = false;

                if (isKeepOpening && isRunningNormally) {
                    return;
                }

                if (null != sendingFuture) {
                    sendingFuture.cancel(true);
                }
                if (null != readingFuture) {
                    readingFuture.cancel(true);
                }
                if (null != serialFuture) {
                    serialFuture.cancel(true);
                }
            }

        }

        private void startSerialPort() {
            isRunningNormally = true;
            serialFuture = TaskFactory.submit(this::executeSerialPort);
        }

        private void startSending(OutputStream os) {
            sendingFuture = TaskFactory.submit(() -> {
                try {
                    sending(os);
                } catch (Throwable e) {
                    if (e instanceof InterruptedException) {
                        return;
                    }
                    Log.e(TAG, "串口模块异常：", e);
                    toast("串口异常: sending->%s", e.getMessage());
                } finally {
                    isRunningNormally = false;
                }
            });
        }

        private void startReading(InputStream is, OutputStream os) {
            readingFuture = TaskFactory.submit(() -> {
                try {
                    reading(is, os);
                } catch (Throwable e) {
                    if (e instanceof InterruptedException) {
                        return;
                    }
                    Log.e(TAG, "串口模块异常：", e);
                    toast("串口异常: reading->%s", e.getMessage());
                } finally {
                    isRunningNormally = false;
                }
            });
        }

        private void executeSerialPort() {
            try {
                com.aill.androidserialport.SerialPort serialPort = new com.aill.androidserialport.SerialPort(file, baudrate, 0);
                try (InputStream is = serialPort.getInputStream();
                     OutputStream os = serialPort.getOutputStream()) {
                    if (isKeepOpening) {
                        startSending(os);
                        startReading(is, os);
                    } else {
                        send(os);
                        reading(is, os);
                    }
                    while (!Thread.interrupted()) {
                        Thread.sleep(1);
                    }
                }
            } catch (Throwable e) {
                if (e instanceof InterruptedException) {
                    return;
                }
                Log.e(TAG, "串口模块异常：", e);
                toast("读卡器异常: %s", e.getMessage());
            } finally {
                isRunningNormally = false;
            }
        }

        private void sending(OutputStream os) throws InterruptedException, IOException {
            while (!Thread.interrupted()) {
                synchronized (lock) {
                    if (isNewCase) {
                        isNewCase = false;
                        send(os);
                    }
                }
                Thread.sleep(1);
            }
        }

        private void reading(InputStream is, OutputStream os) throws InterruptedException, IOException {
            byte[] buffer = new byte[1024];
            int wait = 0;
            OnCmdListener listener;
            Cmd curCmd;
            Cmd nextCmd;
            byte[] cmdData;
            byte[] bizData;
            while (!Thread.interrupted()) {
                // 业务已经停止，丢掉无用数据
                if(!isBizing) {
                    if (is.available() > 0) {
                        is.read(buffer);
                    }
                    Thread.sleep(1);
                    continue;
                }
                // 等待响应
                if(is.available() == 0) {
                    wait++;

                    // 响应超时500毫秒
                    if (wait > 500) {
                        wait = 0;
                        if (!isBizing || !isSended || null == (curCmd = cmdCase.getCurCmd()) || null == (listener = curCmd.getListener())) {
                            Thread.sleep(1);
                            continue;
                        }
                        isSended = false;
                        try {
                            nextCmd = listener.onFail((byte)0);
                        } catch (Exception ef) {
                            LogUtils.error(ef, "receive: error to call onFail on timeout");
                            Thread.sleep(1);
                            continue;
                        }
                        if (!isBizing) {
                            Thread.sleep(1);
                            continue;
                        }
                        cmdCase.setCurCmd(nextCmd);
                        if (null != nextCmd) {
                            //Log.d(TAG, "receive: timeout to next");
                            send(os);
                        } else {
                            Log.d(TAG, "receive: timeout to end");
                        }
                    }

                    // 继续等待响应
                    Thread.sleep(1);
                    continue;
                }

                // 收到响应
                wait = 0;
                isSended = false;
                cmdData = new byte[is.read(buffer)];
                System.arraycopy(buffer, 0, cmdData, 0, cmdData.length);
                //Log.d(TAG, "receive data：" + toHexString(cmdData));

                try {
                    bizData = cmdCase.analysis(cmdData);
                } catch (CmdPackException | CmdFailException e) {// 解码失败
                    //Log.d(TAG, e.getMessage(), e);
                    if (null == (curCmd = cmdCase.getCurCmd()) || null == (listener = curCmd.getListener())) {
                        Log.d(TAG, "receive: fail with next cmd is null to end");
                        continue;
                    }
                    if (!isBizing) {
                        continue;
                    }
                    try {
                        nextCmd = listener.onFail((byte)0);
                    } catch (Exception ef) {
                        LogUtils.error(ef, "receive: error to call onFail on analysis");
                        continue;
                    }
                    if (!isBizing) {
                        continue;
                    }
                    cmdCase.setCurCmd(nextCmd);
                    if (null != nextCmd) {
                        //Log.d(TAG, "receive: fail to next");
                        send(os);
                    } else {
                        Log.d(TAG, "receive: fail to end");
                    }
                    continue;
                } catch (Exception e) {
                    LogUtils.error(e, "receive: error on analysis");
                    continue;
                }

                // 解码成功

                if (null == (curCmd = cmdCase.getCurCmd()) || null == (listener = curCmd.getListener())) {
                    Log.d(TAG, "receive: success with next cmd is null to end");
                    continue;
                }
                if (!isBizing) {
                    continue;
                }
                try {
                    // 执行指令成功后的正常业务
                    nextCmd = listener.onSuccess(bizData);
                } catch (Exception e) {// 指令对应的业务异常时当作失败
                    LogUtils.error(e, "receive: error to call onSuccess on onSuccess");
                    if (!isBizing) {
                        continue;
                    }
                    try {
                        nextCmd = listener.onFail((byte)0);
                    } catch (Exception ef) {
                        LogUtils.error(ef, "receive: error to call onFail on onSuccess");
                        continue;
                    }
                    if (!isBizing) {
                        continue;
                    }
                    cmdCase.setCurCmd(nextCmd);
                    if (null != nextCmd) {
                        Log.d(TAG, "receive: fail to next");
                        send(os);
                    } else {
                        Log.d(TAG, "receive: fail to end");
                    }
                    continue;
                }
                if (!isBizing) {
                    continue;
                }
                // 当前指令业务成功后继续下一个指令
                cmdCase.setCurCmd(nextCmd);
                if (null != nextCmd) {
                    //Log.d(TAG, "receive: success to next");
                    send(os);
                } else {
                    Log.d(TAG, "receive: success to end");
                }
            }
        }

        private void send(OutputStream os) throws IOException {
            if (!isBizing) {
                return;
            }
            Cmd cmd = cmdCase.getCurCmd();
            if (null == cmd) {
                return;
            }
            byte[] data = cmd.data();
            if (null == data) {
                return;
            }
            //Log.d(TAG, "send data：" + toHexString(data));
            os.write(data);
            os.flush();
            isSended = true;
            //Log.d(TAG, "send success");
        }
    }
}
