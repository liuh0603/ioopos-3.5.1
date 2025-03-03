package com.pay.ioopos.support.keyboard.geekmaker;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.FLAG_EDITOR_ACTION;
import static android.view.KeyEvent.FLAG_FALLBACK;
import static android.view.KeyEvent.FLAG_FROM_SYSTEM;
import static com.pay.ioopos.common.AppFactory.dispatchKeyEvent;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.KeyEvent;

import com.geekmaker.paykeyboard.I2C;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.pay.ioopos.App;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.support.keyboard.UsbKeyboard;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 极制键盘
 *
 * @author Moyq5
 * @since 2020/7/22 14:29
 */
public class GmKeyboard implements UsbKeyboard, SerialInputOutputManager.Listener {
    private static final String TAG = GmKeyboard.class.getSimpleName();
    private static final String MODEL = "GeekMaker";
    public static final int DEFAULT_BAUD_RATE = 9600;
    private static final GmKeyboard keyboard = new GmKeyboard();
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private static final String ACTION_USB_PERMISSION = "com.geekmaker.USB_PERMISSION";
    private int baudRate = DEFAULT_BAUD_RATE;

    private static final int STATE_UNKNOWN = 0;
    private static final int STATE_OPENED = 1;
    private static final int STATE_RELEASED = 2;

    private int state = STATE_UNKNOWN;
    private final Object writeLock = new Object();
    private Timer timer = new Timer();
    private boolean refreshRunning = false;

    private UsbDevice device;
    private UsbSerialPort port;
    private UsbDeviceConnection connection;
    private SerialInputOutputManager serialManager;

    private byte lastRequest;
    private byte[] lastDisplayBytes;
    private int lastResponse = -1;

    private static Map<Integer, Integer> keyMap = new HashMap<>();

    static {
        keyMap.put(12, KeyInfo.KEY_MENU.getCode()[0]);
        keyMap.put(28, KeyInfo.KEY_SEARCH.getCode()[0]);
        keyMap.put(29, KeyInfo.KEY_UP.getCode()[0]);
        keyMap.put(30, KeyInfo.KEY_DOWN.getCode()[0]);
        keyMap.put(8, KeyInfo.KEY_MULTIPLY.getCode()[0]);
        keyMap.put(7, KeyInfo.KEY_DIVIDE.getCode()[0]);
        keyMap.put(6, KeyInfo.KEY_SUBTRACT.getCode()[0]);
        keyMap.put(20, KeyInfo.KEY_DELETE.getCode()[0]);
        keyMap.put(5, KeyInfo.KEY_NUM_7.getCode()[0]);
        keyMap.put(4, KeyInfo.KEY_NUM_8.getCode()[0]);
        keyMap.put(3, KeyInfo.KEY_NUM_9.getCode()[0]);
        keyMap.put(2, KeyInfo.KEY_NUM_4.getCode()[0]);
        keyMap.put(1, KeyInfo.KEY_NUM_5.getCode()[0]);
        keyMap.put(16, KeyInfo.KEY_NUM_6.getCode()[0]);
        keyMap.put(31, KeyInfo.KEY_NUM_1.getCode()[0]);
        keyMap.put(15, KeyInfo.KEY_NUM_2.getCode()[0]);
        keyMap.put(14, KeyInfo.KEY_NUM_3.getCode()[0]);
        keyMap.put(9, KeyInfo.KEY_NUM_0.getCode()[0]);
        keyMap.put(19, KeyInfo.KEY_DOT.getCode()[0]);
        keyMap.put(13, KeyInfo.KEY_CANCEL.getCode()[0]);
        keyMap.put(21, KeyInfo.KEY_ADD.getCode()[0]);
        keyMap.put(23, KeyInfo.KEY_ENTER.getCode()[0]);
    }

    private GmKeyboard() {

    }

    /**
     * 获取键盘实例
     * @author Moyq5
     * @since 2020/7/22 14:30
     */
    public static GmKeyboard getInstance() {
        return keyboard;
    }

    @Override
    public void onAttached() {
        if (state == STATE_OPENED) {
            return;
        }
        open();
    }

    @Override
    public void onDetached() {
        if (state == STATE_RELEASED) {
            return;
        }
        UsbManager manager = (UsbManager) App.getInstance().getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> allDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        boolean isDetached = true;
        for (UsbSerialDriver driver: allDrivers) {
            UsbDevice device = driver.getDevice();
            if (device.getVendorId() == 6790 && device.getProductId() == 29987) {
                isDetached = false;
                break;
            }
        }
        if (null != device && isDetached) {
            release();
        }
    }

    @Override
    public void onNewData(byte[] rawData) {

        if (rawData[0] != 6 && rawData[0] != 2) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(rawData);
        while (buffer.remaining() > 0) {
            byte head = buffer.get();
            boolean isResponse = head == 6;
            if (isResponse) {
                head = buffer.get();
            }

            if (head != 2) {
                return;
            }

            int len = buffer.getShort() & 255;
            int lrc = this.lrc(rawData, buffer.position() - 2, buffer.position() + len);
            byte seq = buffer.get();
            int type = buffer.get() & 255;

            //if(state != STATE_OPENED && type!=0x1A) return;

            short errorCode = (short) (buffer.getShort() & 255);
            if (errorCode != 0) return;
            byte[] resp = new byte[len - 4];
            buffer.get(resp);
            if (buffer.get() != 3) {
                return;
            }

            if ((buffer.get() & 255) != lrc) {
                return;
            }

            if (isResponse) {
                // Log.i("KeyboardSDK","get type "+type);
                if (type == 28) {
                    synchronized (writeLock) {
                        lastResponse = seq;
                    }
                } else if (type == 0x16) {
                    state = STATE_OPENED;
                    //keepAlive();
                } else if (type == 0x1A) { //get version info
                    state = STATE_OPENED;
                    //keepAlive();
                }
            } else if (type == 163) {
                int gmKeyCode = resp[1] & 255;
                Log.i(TAG, "onNewData: keyCode=" + gmKeyCode);
                int keyCode = keyMap.get(gmKeyCode);// 转换成应用实际使用的键值
                int action = resp[0] != 1 ? ACTION_UP : ACTION_DOWN;
                KeyEvent keyEvent = new KeyEvent(0, 0, action, keyCode, 0, 0, device.getDeviceId(), gmKeyCode, FLAG_FROM_SYSTEM|FLAG_EDITOR_ACTION|FLAG_FALLBACK);
                dispatchKeyEvent(keyEvent);
            }
        }
    }

    @Override
    public void onRunError(Exception e) {

    }

    private int lrc(byte[] data, int start, int end) {
        int lrc = 0;
        for (int i = start; i <= end; ++i) {
            lrc = (lrc ^ data[i] & 255) & 255;
        }
        return lrc;
    }

    private void keepAlive() {
        if (state == STATE_RELEASED || null == timer) {
            return;
        }
        timer.schedule(new TimerTask() {
            long lastLiveTime = -1;
            @Override
            public void run() {
                if (state == STATE_RELEASED) {
                    cancel();
                    return;
                }
                if (lastLiveTime == -1) {
                    ping();
                } else {
                    long now = System.currentTimeMillis();
                    long duration = now - lastLiveTime;
                    if (duration > 10000) {
                        release();
                    } else if (duration > 2000) {
                        ping();
                    }
                }
                lastLiveTime = System.currentTimeMillis();
            }
        }, 1000, 2000);
    }

    private void ping() {
        // byte[] data = new byte[]{0x11,0x22,0x33,0x44,0x55,0x66,0x02,0x08};
        byte[] data = new byte[8];
        new Random().nextBytes(data);
        sendRequest(data, (byte) 0x16);
    }

    @Override
    public void showMessage(String msg) {
        if (state != STATE_OPENED) {
            return;
        }
        sendRequest((new I2C(msg, false)).toBytes(), (byte) 28);
    }

    private int sendRequest(byte[] data, byte type) {
        synchronized (writeLock) {
            byte[] command = new byte[data.length + 7];
            command[0] = 2;
            int len = data.length + 2;
            command[1] = (byte) (len >> 8 & 255);
            command[2] = (byte) (len & 255);
            int seq = SEQ.getAndIncrement();
            command[3] = (byte) (seq & 255);
            command[4] = type;

            for (int i = 0; i < data.length; ++i) {
                command[5 + i] = data[i];
            }

            command[command.length - 2] = 3;
            command[command.length - 1] = (byte) lrc(command, 1, command.length - 2);
            //Log.d("KeyboardSDK", "write command " + HexDump.dumpHexString(command));

            if (type == 28) {
                lastRequest = command[3];
                lastDisplayBytes = command;
                ensureRefreshRun();
            }
            writeRaw(command);
            if (seq >= 255) SEQ.set(0);
            return seq;
        }
    }

    public void setLED(int setting) {
        sendRequest(new byte[]{(byte) setting}, (byte) 0xA0);
    }

    private void ensureRefreshRun() {
        if (refreshRunning || timer == null) {
            return;
        }
        refreshRunning = true;
        //timer.scheduleAtFixedRate(new RetryTask(), 100, 100);
    }

    private class RetryTask extends TimerTask {
        public void run() {
            synchronized (writeLock) {
                if (lastDisplayBytes == null) {
                    return;
                }

                if (lastRequest == lastResponse) {
                    refreshRunning = false;
                    this.cancel();
                    return;
                }
                writeRaw(lastDisplayBytes);
            }
        }
    }

    private void writeRaw(byte[] data) {
        if (serialManager == null) {
            return;
        }
        serialManager.writeAsync(data);
    }

    private void fetchVersionInfo() {
        sendRequest(new byte[0], (byte) 0x1A);
    }

    private void open() {
        if (state == STATE_OPENED) {
            return;
        }
        List<UsbSerialDriver> drivers = new ArrayList<>();
        UsbManager manager = (UsbManager) App.getInstance().getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> allDrivers = null;
        try {
            allDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        } catch (RuntimeException ignored) {
            // android.os.DeadSystemException
        }
        if (null == allDrivers) {
            return;
        }
        allDrivers.forEach(driver -> {
            UsbDevice device = driver.getDevice();
            if (device.getVendorId() == 6790 && device.getProductId() == 29987) {
                drivers.add(driver);
            }
        });
        tryGetPermisson(drivers);
    }

    private void tryGetPermisson(List<UsbSerialDriver> drivers) {
        if (drivers.size() == 0) {
            Log.d(TAG, "tryGetPermisson: drivers empty");
            return;
        }
        UsbSerialDriver driver = drivers.remove(0);
        UsbDevice device = driver.getDevice();
        UsbManager manager = (UsbManager) App.getInstance().getSystemService(Context.USB_SERVICE);
        if (manager.hasPermission(device)) {
            if (!openDevice(driver)) {
                tryGetPermisson(drivers);
                return;
            }
            return;
        }
        Context applicationContext = App.getInstance().getApplicationContext();
        PendingIntent intent = PendingIntent.getBroadcast(applicationContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                applicationContext.registerReceiver(this, filter);
                String action = intent.getAction();
                if (!ACTION_USB_PERMISSION.equals(action)) {
                    return;
                }
                UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
                if (intent.getBooleanExtra("permission", false)) {
                    if (openDevice(driver)) {
                        drivers.clear();
                        return;
                    }
                }
                Log.w(TAG, "onReceive: permission denied for device " + device);
                tryGetPermisson(drivers);
            }
        };
        applicationContext.registerReceiver(receiver, filter);
        manager.requestPermission(device, intent);
    }

    private boolean openDevice(UsbSerialDriver driver) {
        UsbManager manager = (UsbManager) App.getInstance().getSystemService(Context.USB_SERVICE);
        device = driver.getDevice();
        port = driver.getPorts().get(0);
        connection = manager.openDevice(device);

        try {
            port.open(connection);
            port.setParameters(baudRate, 0, 0, 0);
        } catch (Exception e) {
            // java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.hardware.usb.UsbDeviceConnection.claimInterface(android.hardware.usb.UsbInterface, boolean)' on a null object reference
            Log.e(TAG, "openDevice: ", e);
            return false;
        }
        state = STATE_OPENED;
        serialManager = new SerialInputOutputManager(port, this);
        new Thread(serialManager).start();

        if (null == timer) {
            timer = new Timer();
        }
        showMessage("0.00");
        //keepAlive();
        return true;
    }

    private void release() {
        if (state == STATE_RELEASED) {
            return;
        }
        state = STATE_RELEASED;

        if (serialManager != null) {
            try {
                serialManager.stop();
            } catch (Exception ignored) {

            } finally {
                serialManager = null;
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {

            } finally {
                connection = null;
            }
        }

        if (port != null) {
            try {
                port.close();
            } catch (IOException ignored) {

            } finally {
                port = null;
            }
        }

        if (timer != null) {
            try {
                timer.cancel();
            } catch (Exception ignored) {

            } finally {
                timer = null;
            }
        }
    }

}
