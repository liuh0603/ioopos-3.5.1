package com.aggregate.pay.sanstar.bean;

import com.aggregate.pay.sanstar.enums.NetType;

/**
 * 终端心跳-请求参数
 * @author Moyq5
 * @date 2019年11月18日
 */
public class DevicePantData {

	/**
	 * 在线状态
	 */
	private Boolean online;
	/**
	 * 软件版本
	 */
	private String appVer;
	/**
	 * 系统版本
	 */
	private String sysVer;
	/**
	 * 品牌
	 */
	private String devBrand;
	/**
	 * 型号
	 */
	private String devModel;
	/**
	 * IMEI号
	 */
	private String devImei;
	/**
	 * IP
	 */
	private String devIp;
	/**
	 * MAC
	 */
	private String devMac;
	/**
	 * 网络类型
	 */
	private NetType netType;
	/**
	 * 下行流量(字节)
	 */
	private Long rxKb;
	/**
	 * 上行流量(字节)
	 */
	private Long txKb;
	/**
	 * 开关状态汇总，每位表示一种开关的开关状态，1为开，0为关
	 */
	private Integer switchs;
	/**
	 * 日志信息，一般为最一次错误日志
	 */
	private String logs;

	public Boolean getOnline() {
		return online;
	}

	public void setOnline(Boolean online) {
		this.online = online;
	}

	public String getAppVer() {
		return appVer;
	}

	public void setAppVer(String appVer) {
		this.appVer = appVer;
	}

	public String getSysVer() {
		return sysVer;
	}

	public void setSysVer(String sysVer) {
		this.sysVer = sysVer;
	}

	public String getDevBrand() {
		return devBrand;
	}

	public void setDevBrand(String devBrand) {
		this.devBrand = devBrand;
	}

	public String getDevModel() {
		return devModel;
	}

	public void setDevModel(String devModel) {
		this.devModel = devModel;
	}

	public String getDevImei() {
		return devImei;
	}

	public void setDevImei(String devImei) {
		this.devImei = devImei;
	}

	public String getDevIp() {
		return devIp;
	}

	public void setDevIp(String devIp) {
		this.devIp = devIp;
	}

	public String getDevMac() {
		return devMac;
	}

	public void setDevMac(String devMac) {
		this.devMac = devMac;
	}

	public NetType getNetType() {
		return netType;
	}

	public void setNetType(NetType netType) {
		this.netType = netType;
	}

	public Long getRxKb() {
		return rxKb;
	}

	public void setRxKb(Long rxKb) {
		this.rxKb = rxKb;
	}

	public Long getTxKb() {
		return txKb;
	}

	public void setTxKb(Long txKb) {
		this.txKb = txKb;
	}

	public Integer getSwitchs() {
		return switchs;
	}

	public void setSwitchs(Integer switchs) {
		this.switchs = switchs;
	}

	public String getLogs() {
		return logs;
	}

	public void setLogs(String logs) {
		this.logs = logs;
	}

}
