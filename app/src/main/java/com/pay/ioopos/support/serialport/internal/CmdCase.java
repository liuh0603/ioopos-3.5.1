package com.pay.ioopos.support.serialport.internal;

import androidx.lifecycle.LifecycleOwner;

import com.pay.ioopos.common.TaskFactory;

import java.util.concurrent.Future;

/**
 * 串口指令模块实现
 * @author    Moyq5
 * @since  2020/12/30 17:29
 */
public abstract class CmdCase extends CmdCaseAbstract {
    private static Future<?> future;

    public CmdCase(String path, int baudrate) {
        super(path, baudrate);
    }

    public CmdCase(String path, int baudrate, Cmd cmd) {
        super(path, baudrate, cmd);
    }


    @Override
    protected void onStart(LifecycleOwner owner) {
        if (null != future) {
            future.cancel(true);
        }
        future = TaskFactory.submit(() -> SerialPortFactory.getSerialPort(this.getPath(), this.getBaudrate()).start(this));
    }

    @Override
    protected void onStop(LifecycleOwner owner) {
        if (null != future) {
            future.cancel(true);
        }
        TaskFactory.execute(() ->  SerialPortFactory.getSerialPort(this.getPath(), this.getBaudrate()).stop());
    }


}
