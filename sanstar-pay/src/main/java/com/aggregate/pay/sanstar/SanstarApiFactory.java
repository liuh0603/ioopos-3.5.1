package com.aggregate.pay.sanstar;

import javax.servlet.http.HttpServletRequest;

import com.aggregate.pay.sanstar.bean.CardActiveData;
import com.aggregate.pay.sanstar.bean.CardActiveResult;
import com.aggregate.pay.sanstar.bean.CardAuthResult;
import com.aggregate.pay.sanstar.bean.CardBalanceData;
import com.aggregate.pay.sanstar.bean.CardBalanceResult;
import com.aggregate.pay.sanstar.bean.CardBlackData;
import com.aggregate.pay.sanstar.bean.CardBlackResult;
import com.aggregate.pay.sanstar.bean.CardCancelData;
import com.aggregate.pay.sanstar.bean.CardCancelResult;
import com.aggregate.pay.sanstar.bean.CardChargeConfirmData;
import com.aggregate.pay.sanstar.bean.CardChargeConfirmResult;
import com.aggregate.pay.sanstar.bean.CardChargeData;
import com.aggregate.pay.sanstar.bean.CardChargeResult;
import com.aggregate.pay.sanstar.bean.CardCreateData;
import com.aggregate.pay.sanstar.bean.CardCreateResult;
import com.aggregate.pay.sanstar.bean.CardLockData;
import com.aggregate.pay.sanstar.bean.CardLockResult;
import com.aggregate.pay.sanstar.bean.CardQueryData;
import com.aggregate.pay.sanstar.bean.CardQueryResult;
import com.aggregate.pay.sanstar.bean.PayNotifyData;
import com.aggregate.pay.sanstar.bean.PayOrderData;
import com.aggregate.pay.sanstar.bean.PayOrderResult;
import com.aggregate.pay.sanstar.bean.PayQueryData;
import com.aggregate.pay.sanstar.bean.PayQueryResult;
import com.aggregate.pay.sanstar.bean.PayRepealData;
import com.aggregate.pay.sanstar.bean.PayUploadData;
import com.aggregate.pay.sanstar.bean.PayUploadResult;
import com.aggregate.pay.sanstar.bean.QrcodeAccountActiveData;
import com.aggregate.pay.sanstar.bean.QrcodeAddData;
import com.aggregate.pay.sanstar.bean.QrcodeNotifyData;
import com.aggregate.pay.sanstar.bean.RefundOrderData;
import com.aggregate.pay.sanstar.bean.RefundOrderResult;
import com.aggregate.pay.sanstar.bean.RefundQueryData;
import com.aggregate.pay.sanstar.bean.RefundQueryResult;
import com.aggregate.pay.sanstar.bean.StatisticsOverviewResult;
import com.aggregate.pay.sanstar.bean.TerminalBindData;
import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.bean.TerminalInfoResult;
import com.aggregate.pay.sanstar.bean.DevicePantData;
import com.aggregate.pay.sanstar.bean.WxFaceBlackData;
import com.aggregate.pay.sanstar.bean.WxFaceBlackResult;
import com.aggregate.pay.sanstar.bean.WxFaceMerchResult;
import com.aggregate.pay.sanstar.bean.WxFaceUserData;
import com.aggregate.pay.sanstar.bean.WxFaceUserResult;
import com.aggregate.pay.sanstar.bean.WxSchoolSdkResult;
import com.aggregate.pay.sanstar.support.Client;
import com.aggregate.pay.sanstar.support.Config;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.HttpClient;
import com.aggregate.pay.sanstar.support.Merch;
import com.aggregate.pay.sanstar.support.client.CardActive;
import com.aggregate.pay.sanstar.support.client.CardAuth;
import com.aggregate.pay.sanstar.support.client.CardBalance;
import com.aggregate.pay.sanstar.support.client.CardBlack;
import com.aggregate.pay.sanstar.support.client.CardCancel;
import com.aggregate.pay.sanstar.support.client.CardCharge;
import com.aggregate.pay.sanstar.support.client.CardChargeConfirm;
import com.aggregate.pay.sanstar.support.client.CardCreate;
import com.aggregate.pay.sanstar.support.client.CardCreateQuery;
import com.aggregate.pay.sanstar.support.client.CardLock;
import com.aggregate.pay.sanstar.support.client.PayNotify;
import com.aggregate.pay.sanstar.support.client.PayOrder;
import com.aggregate.pay.sanstar.support.client.PayQuery;
import com.aggregate.pay.sanstar.support.client.PayRepeal;
import com.aggregate.pay.sanstar.support.client.PayUpload;
import com.aggregate.pay.sanstar.support.client.QrcodeAccountActive;
import com.aggregate.pay.sanstar.support.client.QrcodeAdd;
import com.aggregate.pay.sanstar.support.client.QrcodeNotify;
import com.aggregate.pay.sanstar.support.client.RefundOrder;
import com.aggregate.pay.sanstar.support.client.RefundQuery;
import com.aggregate.pay.sanstar.support.client.StatisticsOverview;
import com.aggregate.pay.sanstar.support.client.StatisticsOverviewV2;
import com.aggregate.pay.sanstar.support.client.StatisticsPay;
import com.aggregate.pay.sanstar.support.client.StatisticsRefund;
import com.aggregate.pay.sanstar.support.client.TerminalBind;
import com.aggregate.pay.sanstar.support.client.TerminalCheck;
import com.aggregate.pay.sanstar.support.client.TerminalInfo;
import com.aggregate.pay.sanstar.support.client.TerminalPant;
import com.aggregate.pay.sanstar.support.client.TerminalSdk;
import com.aggregate.pay.sanstar.support.client.TerminalUnbind;
import com.aggregate.pay.sanstar.support.client.WxFaceBlack;
import com.aggregate.pay.sanstar.support.client.WxFaceMerch;
import com.aggregate.pay.sanstar.support.client.WxFaceUser;
import com.aggregate.pay.sanstar.support.client.WxSchoolSdk;

/**
 * 盛思达支付接口工具类，相关配置和接口调用从此类开始
 * @author Moyq5
 * @date 2017年9月29日
 */
public abstract class SanstarApiFactory {

	public static void config(Config config, HttpClient httpClient) {
		Factory.setConfig(config);
		Factory.setHttpClient(httpClient);
	}

	@SuppressWarnings("unchecked")
	public static Client<TerminalBindData, TerminalBindResult> terminalBind(Merch merch) {
		return (Client<TerminalBindData, TerminalBindResult>)Factory.getClient(TerminalBind.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<Void, TerminalBindResult> terminalCheck(Merch merch) {
		return (Client<Void, TerminalBindResult>)Factory.getClient(TerminalCheck.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<Void, Void> terminalUnbind(Merch merch) {
		return (Client<Void, Void>)Factory.getClient(TerminalUnbind.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<Void, TerminalInfoResult> terminalInfo(Merch merch) {
		return (Client<Void, TerminalInfoResult>)Factory.getClient(TerminalInfo.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<DevicePantData, Void> terminalPant(Merch merch) {
		return (Client<DevicePantData, Void>)Factory.getClient(TerminalPant.class, merch);
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public static Client<Void, WxSchoolSdkResult> terminalSdk(Merch merch) {
		return (Client<Void, WxSchoolSdkResult>)Factory.getClient(TerminalSdk.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<PayOrderData, PayOrderResult> payOrder(Merch merch) {
		return (Client<PayOrderData, PayOrderResult>)Factory.getClient(PayOrder.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<PayUploadData, PayUploadResult> payUpload(Merch merch) {
		return (Client<PayUploadData, PayUploadResult>)Factory.getClient(PayUpload.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<PayQueryData, PayQueryResult> payQuery(Merch merch) {
		return (Client<PayQueryData, PayQueryResult>)Factory.getClient(PayQuery.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<PayRepealData, Void> payRepeal(Merch merch) {
		return (Client<PayRepealData, Void>)Factory.getClient(PayRepeal.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<HttpServletRequest, PayNotifyData> payNotify(Merch merch) {
		return (Client<HttpServletRequest, PayNotifyData>)Factory.getClient(PayNotify.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<RefundOrderData, RefundOrderResult> refundOrder(Merch merch) {
		return (Client<RefundOrderData, RefundOrderResult>)Factory.getClient(RefundOrder.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<RefundQueryData, RefundQueryResult> refundQuery(Merch merch) {
		return (Client<RefundQueryData, RefundQueryResult>)Factory.getClient(RefundQuery.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<Void, StatisticsOverviewResult> statisticsOverview(Merch merch) {
		return (Client<Void, StatisticsOverviewResult>)Factory.getClient(StatisticsOverview.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<Void, String> statisticsOverviewV2(Merch merch) {
		return (Client<Void, String>)Factory.getClient(StatisticsOverviewV2.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<String, String> statisticsPay(Merch merch) {
		return (Client<String, String>)Factory.getClient(StatisticsPay.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<String, String> statisticsRefund(Merch merch) {
		return (Client<String, String>)Factory.getClient(StatisticsRefund.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<Void, WxSchoolSdkResult> wxSchoolSdk(Merch merch) {
		return (Client<Void, WxSchoolSdkResult>)Factory.getClient(WxSchoolSdk.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<String, WxFaceMerchResult> wxFaceMerch(Merch merch) {
		return (Client<String, WxFaceMerchResult>)Factory.getClient(WxFaceMerch.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<WxFaceUserData, WxFaceUserResult> wxFaceUser(Merch merch) {
		return (Client<WxFaceUserData, WxFaceUserResult>)Factory.getClient(WxFaceUser.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<WxFaceBlackData, WxFaceBlackResult> wxFaceBlack(Merch merch) {
		return (Client<WxFaceBlackData, WxFaceBlackResult>)Factory.getClient(WxFaceBlack.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<Void, CardAuthResult> cardAuth(Merch merch) {
		return (Client<Void, CardAuthResult>)Factory.getClient(CardAuth.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<CardCreateData, CardCreateResult> cardCreate(Merch merch) {
		return (Client<CardCreateData, CardCreateResult>)Factory.getClient(CardCreate.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<CardQueryData, CardQueryResult> cardCreateQuery(Merch merch) {
		return (Client<CardQueryData, CardQueryResult>)Factory.getClient(CardCreateQuery.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<CardActiveData, CardActiveResult> cardActive(Merch merch) {
		return (Client<CardActiveData, CardActiveResult>)Factory.getClient(CardActive.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<CardBalanceData, CardBalanceResult> cardBalance(Merch merch) {
		return (Client<CardBalanceData, CardBalanceResult>)Factory.getClient(CardBalance.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<CardBlackData, CardBlackResult> cardBlack(Merch merch) {
		return (Client<CardBlackData, CardBlackResult>)Factory.getClient(CardBlack.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<CardLockData, CardLockResult> cardLock(Merch merch) {
		return (Client<CardLockData, CardLockResult>)Factory.getClient(CardLock.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<CardChargeData, CardChargeResult> cardCharge(Merch merch) {
		return (Client<CardChargeData, CardChargeResult>)Factory.getClient(CardCharge.class, merch);
	}

	@SuppressWarnings("unchecked")
	public static Client<CardChargeConfirmData, CardChargeConfirmResult> cardChargeConfirm(Merch merch) {
		return (Client<CardChargeConfirmData, CardChargeConfirmResult>)Factory.getClient(CardChargeConfirm.class, merch);
	}
	
	@SuppressWarnings("unchecked")
	public static Client<CardCancelData, CardCancelResult> cardCancel(Merch merch) {
		return (Client<CardCancelData, CardCancelResult>)Factory.getClient(CardCancel.class, merch);
	}
	
	@Deprecated
	@SuppressWarnings("unchecked")
	public static Client<QrcodeAccountActiveData, Void> qrcodeAccountActive(Merch merch) {
		return (Client<QrcodeAccountActiveData, Void>)Factory.getClient(QrcodeAccountActive.class, merch);
	}
	
	@Deprecated
	@SuppressWarnings("unchecked")
	public static Client<QrcodeAddData, Void> qrcodeAdd(Merch merch) {
		return (Client<QrcodeAddData, Void>)Factory.getClient(QrcodeAdd.class, merch);
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public static Client<QrcodeNotifyData, Void> qrcodeNotify(Merch merch) {
		return (Client<QrcodeNotifyData, Void>)Factory.getClient(QrcodeNotify.class, merch);
	}
	
}
