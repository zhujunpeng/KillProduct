package com.zjp.service;

import org.springframework.stereotype.Service;

import com.zjp.bean.SecOrder;
import com.zjp.bean.SecProductInfo;
import com.zjp.exception.SellException;

public interface SecKillService {

	/**
	 * 将订单信息保存在redis中
	 * @param productId
	 * @param secOrder
	 * @throws Exception 
	 */
	public long orderProductMockDiffUser(String productId, SecOrder secOrder) throws Exception;

	/**
	 * 在redis中刷新库存
	 * @param productId
	 * @return
	 * @throws SellException 
	 */
	public SecProductInfo refreshStock(String productId) throws SellException;
		

}
