package com.zjp.util;

import java.util.Random;

import com.zjp.bean.ProductInfo;
import com.zjp.bean.SecOrder;

public class SecUtils {
	
	/**
	 * 创建虚拟订单
	 */
	public static SecOrder createDummyOrder(ProductInfo productInfo) {
		String key = KeyUtil.getUniqueKey();
		SecOrder secOrder = new SecOrder();
		secOrder.setId(key);
		secOrder.setUserId("userId="+key);
		secOrder.setProductId(productInfo.getProductId());
		secOrder.setProductPrice(productInfo.getProductPrice());
		secOrder.setAmount(productInfo.getProductPrice());
		return secOrder;
	}

	/**
	 * 支付。。。
	 * @return
	 */
	public static boolean dummyPay() {
		Random random = new Random();
		int result = random.nextInt(1000) % 2;
		if (result == 0) {
			return true;
		}
		return false;
	}
}
