package com.pay.ioopos.support.serialport.internal;

import com.pay.ioopos.support.scan.ScanCaseAbstract;

/**
 * 
 * @author    Moyq5
 * @since  2021/3/25 17:51
 */
public abstract class CmdCaseAbstract extends ScanCaseAbstract {
    private Cmd rootCmd;
    private Cmd curCmd;
    private String path;
    private int baudrate;
    public CmdCaseAbstract(String path, int baudrate) {
        this.path = path;
        this.baudrate = baudrate;
    }
    public CmdCaseAbstract(String path, int baudrate, Cmd rootCmd) {
        this(path, baudrate);
        this.rootCmd = rootCmd;
        this.curCmd = rootCmd;
    }

    public Cmd getRootCmd() {
        return rootCmd;
    }

    public void setRootCmd(Cmd cmd) {
        this.rootCmd = cmd;
        this.curCmd = cmd;
    }

    protected Cmd getCurCmd() {
        return curCmd;
    }

    protected void setCurCmd(Cmd curCmd) {
        this.curCmd = curCmd;
    }

    public String getPath() {
        return path;
    }

    public int getBaudrate() {
        return baudrate;
    }

    /**
     * 从指令数据包解析出业务数据部分内容
     * @author  Moyq5
     * @since    2020/12/30 19:04
     * @param   data 指令数据
     * @return 业务数据
     */
    public abstract byte[] analysis(byte[] data);

}
