package com.zjp.util;

import java.util.Random;

public class KeyUtil {
	
	/**
	 * 获取唯一的key值
	 * @return
	 */
	public static synchronized String getUniqueKey() {
		Random random = new Random();
		Integer num = random.nextInt(100000);
		return num.toString()+System.currentTimeMillis();
	}

}
