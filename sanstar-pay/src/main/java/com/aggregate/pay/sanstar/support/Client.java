package com.aggregate.pay.sanstar.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aggregate.pay.sanstar.Result;

/**
 * 接口API操作类
 * @author Moyq5
 * @date 2017年9月29日
 */
public interface Client<D, R> {
	
	final static Logger log = LoggerFactory.getLogger(Client.class);
	
	Result<R> execute(D data);

}