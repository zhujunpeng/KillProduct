package com.zjp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zjp.bean.ProductInfo;
import com.zjp.bean.SecProductInfo;
import com.zjp.exception.SellException;

/**
 * 分布式乐观锁
 * 
 * 线程进来之后先执行redis的setnx，如果key存在就返回0，否则返回1，返回1的意思就是拿到了锁，开始执行代码，执行完成之后将key删除就是解锁
 * 
 * 但是这里就会存在两个问题，
 * 1、存在死锁，就是一个线程拿到锁之后，解锁之前出现bug，导致锁无法释放出来，下一个线程进来之后就一直等到上一个锁释放。这个问题的解决方案就是给锁加上
 * 超时时间，超过这个时间之后无论如何都要将锁释放出来，但是又会出现第二个问题，
 * 2、在超时的情况下，多个线程同时等待锁释放出来，然后就会竞争拿到锁，此时
 * 就会出现线程不安全，解决方案就是使用redis的getandset方法，其中一个线程拿到锁之后立刻将value值改变，同时将oldvalue与原来的值比较
 * 使用乐观锁的方式解决多线程锁竞争的方式锁的安全性
 * 
 * @author zjp
 *
 */
@Component
public class RedisLock {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ProductService productService;

	/**
	 * 加锁
	 * 
	 * @param key  商品id
	 * @param value 当前时间+超时时间
	 * @return
	 */
	public boolean lock(String key, String value) {

		// setIfAbsent对应redis中的setnx，key存在的话返回false，不存在返回true
		if (redisTemplate.opsForValue().setIfAbsent(key, value)) {
			return true;
		}
		// 两个问题，Q1超时时间
		String currentValue = redisTemplate.opsForValue().get(key);
		if (!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()) {
			// 在线程超时的时候，多个线程争抢锁的问题
			String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
			if (!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 释放锁
	 * 
	 * @param key
	 * @param value
	 */
	public void unlock(String key, String value) {
		try {
			String currentValue = redisTemplate.opsForValue().get(key);
			if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
				redisTemplate.opsForValue().getOperations().delete(key);
			}
		} catch (Exception e) {
			logger.error("redis分布上锁解锁异常, {}", e);
		}
	}

	/**
	 * 刷新redis中某一个商品的库存
	 * @param productId
	 * @return
	 * @throws SellException
	 */
	public SecProductInfo refreshStock(String productId) throws SellException {
		SecProductInfo secProductInfo = new SecProductInfo();
		// 从数据库中查询商品
		ProductInfo productInfo = productService.findOne(productId);
		if (productId == null) {
			throw new SellException(203, "秒杀商品不存在");
		}
		try {
			// 设置redis中某个商品库存
			redisTemplate.opsForValue().set("stock"+productInfo.getProductId(), String.valueOf(productInfo.getProductStock()));
			String value = redisTemplate.opsForValue().get("stock"+productInfo.getProductId());
			secProductInfo.setProductId(productId);
			secProductInfo.setStock(value);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return secProductInfo;
	}

}
