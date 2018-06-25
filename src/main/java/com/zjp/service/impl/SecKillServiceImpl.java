package com.zjp.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.mysql.jdbc.log.Log;
import com.zjp.bean.SecOrder;
import com.zjp.bean.SecProductInfo;
import com.zjp.exception.SellException;
import com.zjp.mapper.SecOrderMapper;
import com.zjp.service.RedisLock;
import com.zjp.service.SecKillService;

@Service
public class SecKillServiceImpl implements SecKillService{
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RedisLock redisLock;
	
	@Autowired
	private SecOrderMapper secOrderService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RedisTemplate<String, SecOrder> redisTemplate;

	private static final int TIMEOUT = 10 * 1000;

	/**
	 * 将订单信息保存到redis中
	 */
	public long orderProductMockDiffUser(String productId, SecOrder secOrder) throws Exception {
		// 加锁 setnx
		long orderSize;
		long time = System.currentTimeMillis() + TIMEOUT;
		// 存在请求堵塞在这里
		boolean lock = redisLock.lock(productId, String.valueOf(time));
		if (!lock) {
			throw new SellException(200, "哎呦喂，人太多了");
		}
		// 获取库存数量
		int number = Integer.parseInt(stringRedisTemplate.opsForValue().get(productId));
		if(number >= 2000) {
			throw new SellException(150, "抢购结束啦！！");
		}
		// 仓库数量减一
		stringRedisTemplate.opsForValue().increment("stock"+productId, 1);
		// 向redis中加入订单
		orderSize = redisTemplate.opsForSet().add("order"+productId, secOrder);
		if(orderSize >= 1000) {
			// 订单信息持久化,多线程写入数据库(效率从单线程的9000s提升到了9ms)
			Set<SecOrder> members = redisTemplate.opsForSet().members("order"+productId);
			final List<SecOrder> memberList = new ArrayList<SecOrder>(members);
			final CountDownLatch countDownLatch = new CountDownLatch(4);
			new Thread(new Runnable() {
				public void run() {
					 for (int i = 0; i <memberList.size() /4 ; i++) {
	                       secOrderService.save(memberList.get(i));
	                       countDownLatch.countDown();
	                   }
				}
			},"therad1").start();
			new Thread(new Runnable() {
				
				public void run() {
					for (int i = memberList.size() /4; i <memberList.size() /2 ; i++) {
	                       secOrderService.save(memberList.get(i));
	                       countDownLatch.countDown();
	                   }
				}
			},"therad2").start();
			new Thread(new Runnable() {
				
				public void run() {
					for (int i = memberList.size() /2; i <memberList.size()*3/4 ; i++) {
	                       secOrderService.save(memberList.get(i));
	                       countDownLatch.countDown();
	                   }
				}
			},"therad3").start();
			new Thread(new Runnable() {
				
				public void run() {
					for (int i = memberList.size() *3/4; i <memberList.size() ; i++) {
	                       secOrderService.save(memberList.get(i));
	                       countDownLatch.countDown();
	                   }
				}
			},"therad4").start();
			countDownLatch.await();
			log.info("订单持久化成功~~");
		}
		// 释放锁
		redisLock.unlock(productId, String.valueOf(time));
		return orderSize;
	}
	/**
	 * 刷新某一个商品的
	 * @throws SellException 
	 */
	public SecProductInfo refreshStock(String productId) throws SellException {
		return redisLock.refreshStock(productId);
	}

}
