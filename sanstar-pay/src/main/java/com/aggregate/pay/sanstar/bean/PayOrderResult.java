package com.aggregate.pay.sanstar.bean;

import java.util.Date;
import java.util.List;

import com.aggregate.pay.sanstar.enums.PayStatus;

/**
 * 统一支付接口－响应参数
 * @author Moyq5
 * @date 2018年9月3日
 */
public class PayOrderResult {

	/**
	 * 商户订单号，必填
	 */
	private String cusOrderNo;
	/**
	 * 平台订单号，必填
	 */
	private String orderNo;
	/**
	 * 渠道订单号，选填，可多个
	 */
	private List<String> supOrderNo;
	/**
	 * 支付地址。
	 * 扫码支付时为二维码内容，商户生成二维码客户扫码进行付款；
	 * 网关支付时为支付网关地址，客户访问该地址跳转到支付网关完成支付
	 */
	private String payUrl;
	/**
	 * 支付状态，必填
	 */
	private PayStatus payStatus;
	/**
	 * 支付状态描述
	 */
	private String payDesc;
	/**
	 * 支付完成时间，选填
	 */
	private Date payTime;
	/**
	 * 订单金额：分，必填
	 */
	private Integer amount;
	/**
	 * 实付金额：分，必填
	 */
	private Integer payAmount;
	/**
	 * 余额：分，必填
	 */
	private Integer remainAmount;
	/**
	 * 附加参数，JSON格式，可能携带其它业务的参数，具体由内部定义
	 */
	private String others;
	public String getCusOrderNo() {
		return cusOrderNo;
	}
	public void setCusOrderNo(String cusOrderNo) {
		this.cusOrderNo = cusOrderNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getPayUrl() {
		return payUrl;
	}
	public void setPayUrl(String payUrl) {
		this.payUrl = payUrl;
	}
	public PayStatus getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(PayStatus payStatus) {
		this.payStatus = payStatus;
	}
	public Date getPayTime() {
		return payTime;
	}
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public Integer getRemainAmount() {
		return remainAmount;
	}
	public void setRemainAmount(Integer remainAmount) {
		this.remainAmount = remainAmount;
	}
	public Integer getPayAmount() {
		return payAmount;
	}
	public void setPayAmount(Integer payAmount) {
		this.payAmount = payAmount;
	}
	public String getPayDesc() {
		return payDesc;
	}
	public void setPayDesc(String payDesc) {
		this.payDesc = payDesc;
	}
	public List<String> getSupOrderNo() {
		return supOrderNo;
	}
	public void setSupOrderNo(List<String> supOrderNo) {
		this.supOrderNo = supOrderNo;
	}
	public String getOthers() {
		return others;
	}
	public void setOthers(String others) {
		this.others = others;
	}
}
