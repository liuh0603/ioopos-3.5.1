package com.aggregate.pay.sanstar.support;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aggregate.pay.sanstar.Data;
import com.aggregate.pay.sanstar.NotifyData;
import com.aggregate.pay.sanstar.support.utils.JSON;
import com.aggregate.pay.sanstar.support.utils.MD5;
import com.aggregate.pay.sanstar.support.utils.SHA1;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 盛思达专属工具类
 * @author Moyq5
 * @date 2017年9月29日
 */
public class Tool {
	
	private static Logger log = LoggerFactory.getLogger(Tool.class);
	
	public static String sign(Data data, String key) {
		return sign(JSON.toString(data), key);
	}
	
	public static String sign(NotifyData data, String key) {
		return sign(JSON.toString(data), key);
	}
	
	public static String sign(String jsonString, String key) {
		TreeMap<String, Object> map = JSON.toObject(jsonString, new TypeReference<TreeMap<String, Object>>() {
		});
		return sign(map, key);
	}
	
	private static String sign(TreeMap<String, Object> map, String key) {
		map.remove("sign");
		map.put("key", key);
		StringBuffer sb = new StringBuffer();
		Object value;
		for (Map.Entry<String, Object> entry: map.entrySet()) {
			value = entry.getValue();
			if (null == value || (value instanceof String && ((String)value).isEmpty())) {
				continue;
			}
			sb.append(entry.getKey() + "=" + value + "&");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		
		String str = sb.toString();
		log.debug("签名内容：{}", str);
		String sign = MD5.MD5Encode(SHA1.encrypt(str));
		log.debug("签名值：{}", sign);
		
		return sign;
	}
	
}