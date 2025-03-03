package com.pay.ioopos.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 
 * @author Moyq5
 * @since 2021年5月28日
 */
public abstract class BigDecimalUtils {

	/**
	 * 分转元
	 * @param fen 分
	 * @return 元
	 */
	public static BigDecimal fenToYuan(Integer fen) {
		return new BigDecimal(String.valueOf(fen)).divide(new BigDecimal("100")).setScale(2, RoundingMode.DOWN);
	}
	
	/**
	 * 元转分
	 * @param yuan 元
	 * @return 分
	 */
	public static Integer yuanToFen(BigDecimal yuan) {
		return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.DOWN).intValue();
	}
}
