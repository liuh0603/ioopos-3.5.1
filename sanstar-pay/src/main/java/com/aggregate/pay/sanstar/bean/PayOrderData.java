package com.aggregate.pay.sanstar.bean;

import java.util.Date;

import com.aggregate.pay.sanstar.enums.PayMethod;
import com.aggregate.pay.sanstar.enums.PayMode;
import com.aggregate.pay.sanstar.enums.PayType;

/**
 * 统一支付接口－请求参数
 * @author Moyq5
 * @date 2017年9月29日
 */
public class PayOrderData {

	/**
	 * 商户自定义订单号，同一商户唯 一，长20，必填
	 */
	private String cusOrderNo;
	/**
	 * 商品名称，长50，选填
	 */
	private String goodsName;
	/**
	 * 交易金额，单位分，必填
	 */
	private Integer amount;
	/**
	 * 备注，选填
	 */
	private String remark;
	/**
	 * 支付结果异步通知地址，选填
	 */
	private String notifyUrl;
	/**
	 * 支付结果同步通知地址，选填
	 */
	private String returnUrl;
	/**
	 * 交易时间，必填
	 */
	private Date orderTime;
	/**
	 * 支付类型，必填
	 */
	private PayType payType;
	/**
	 * 支付场景，必填
	 */
	private PayMethod payMethod;
	/**
	 * 扣款模式，选填
	 */
	private PayMode payMode;
	/**
	 * 附加参数，json格式
	 */
	private String cusOthers;
	/**
	 * 授权码，即付款码，条码支付时必填
	 */
	private String authCode;
	/**
	 * 微信公众号支付时为：公众号的AppId；微信App支付是APP的AppId
	 */
	private String appId;
	
	/**
	 * 微信公众号支付时，为用户openId；支付宝生活号支付时为用户uuid;
	 */
	private String openId;
	
	public String getCusOrderNo() {
		return cusOrderNo;
	}

	public void setCusOrderNo(String cusOrderNo) {
		this.cusOrderNo = cusOrderNo;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	public PayType getPayType() {
		return payType;
	}

	public void setPayType(PayType payType) {
		this.payType = payType;
	}

	public PayMethod getPayMethod() {
		return payMethod;
	}

	public void setPayMethod(PayMethod payMethod) {
		this.payMethod = payMethod;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getCusOthers() {
		return cusOthers;
	}

	public void setCusOthers(String cusOthers) {
		this.cusOthers = cusOthers;
	}

	public PayMode getPayMode() {
		return payMode;
	}

	public void setPayMode(PayMode payMode) {
		this.payMode = payMode;
	}
}
