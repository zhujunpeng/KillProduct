package com.zjp.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.zjp.bean.SecOrder;
import com.zjp.bean.SecProductInfo;
import com.zjp.dao.SecKillService;
import com.zjp.dao.SecOrderService;
import com.zjp.exception.SellException;
import com.zjp.service.RedisLock;

@Service
public class SecKillServiceImpl implements SecKillService{
	
	@Autowired
	private RedisLock redisLock;
	
	@Autowired
	private SecOrderService secOrderService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private RedisTemplate<String, SecOrder> redisTemplate;

	private static final int TIMEOUT = 10 * 1000;

	public void orderProductMockDiffUser(String productId, SecOrder secOrder) throws Exception {
		// 加锁 setnx
		long orderSize;
		long time = System.currentTimeMillis() + TIMEOUT;
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
		}
	}

	public SecProductInfo refreshStock(String productId) {
		// TODO Auto-generated method stub
		return null;
	}

}
