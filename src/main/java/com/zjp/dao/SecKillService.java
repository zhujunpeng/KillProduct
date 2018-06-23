package com.zjp.dao;

import org.springframework.stereotype.Service;

import com.zjp.bean.SecOrder;
import com.zjp.bean.SecProductInfo;

public interface SecKillService {

	/**
	 * 将订单信息保存在redis中
	 * @param productId
	 * @param secOrder
	 * @throws Exception 
	 */
	public void orderProductMockDiffUser(String productId, SecOrder secOrder) throws Exception;

	/**
	 * 在redis中刷新库存
	 * @param productId
	 * @return
	 */
	public SecProductInfo refreshStock(String productId);
		

}
