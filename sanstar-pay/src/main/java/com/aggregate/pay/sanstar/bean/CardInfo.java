package com.aggregate.pay.sanstar.bean;

import com.aggregate.pay.sanstar.enums.CardStatus;

/**
 * 卡信息
 * @author Moyq5
 * @date 2020年9月27日
 */
public class CardInfo {

	/**
	 * 卡id
	 */
	private Long cardId;
	/**
	 * 卡号
	 */
	private String cardNo;
	/**
	 * 卡序列号（卡内值）
	 */
	private String cardUid;
	/**
	 * 旧卡序列号（可能来自旧卡换的卡）
	 */
	private String prevUid;
	/**
	 * 部门、职务、班级
	 */
	private String userGroup;
	/**
	 * 工号、学号、证件号
	 */
	private String userNo;
	/**
	 * 员工姓名、学生姓名
	 */
	private String userName;
	/**
	 * 余额(分)
	 */
	private Integer balance;
	/**
	 * 卡状态
	 */
	private CardStatus status;
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getCardUid() {
		return cardUid;
	}
	public void setCardUid(String cardUid) {
		this.cardUid = cardUid;
	}
	public String getPrevUid() {
		return prevUid;
	}
	public void setPrevUid(String prevUid) {
		this.prevUid = prevUid;
	}
	public String getUserGroup() {
		return userGroup;
	}
	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Integer getBalance() {
		return balance;
	}
	public void setBalance(Integer balance) {
		this.balance = balance;
	}
	public CardStatus getStatus() {
		return status;
	}
	public void setStatus(CardStatus status) {
		this.status = status;
	}
	public Long getCardId() {
		return cardId;
	}
	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}
	
}
