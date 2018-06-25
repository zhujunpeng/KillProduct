package com.zjp.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zjp.bean.ProductInfo;
import com.zjp.bean.SecOrder;
import com.zjp.bean.SecProductInfo;
import com.zjp.exception.SellException;
import com.zjp.service.SecKillService;
import com.zjp.util.SecUtils;

@RestController
@RequestMapping("/skill")
public class SecKillController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SecKillService secKillService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RedisTemplate<String, SecOrder> redisTemplate;

	/**
	 * 下单，同时将订单信息保存在redis中，随后将数据持久化
	 * 
	 * @param productId
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("/order/{productId}")
	public String skill(@PathVariable String productId) throws Exception {
		// 判断是否抢光
		int amount = Integer.valueOf(stringRedisTemplate.opsForValue().get("stock" + productId));
		if (amount >= 2000) {
			return "不好意思，抢光了~";
		}
		// 初始化抢购商品信息，创建虚拟订单。
		ProductInfo productInfo = new ProductInfo(productId);
		// 创建订单
		SecOrder secOrder = SecUtils.createDummyOrder(productInfo);
		// 付款 付款时校验库存，如果工程redis存储订单信息，库存+1
		if (!SecUtils.dummyPay()) {
			log.error("付款慢啦抢购失败，再接再厉哦");
			return "抢购失败，再接再厉哦";
		}
		log.info("抢购成功 商品id=:" + productId);
		// 订单信息保存在redis中
		secKillService.orderProductMockDiffUser(productId, secOrder);
		return "订单数量: " + redisTemplate.opsForSet().size("order" + productId) + "  剩余数量:"
				+ (2000 - Integer.valueOf(stringRedisTemplate.opsForValue().get("stock" + productId)));
	}

	@RequestMapping("/refresh/{productId}")
	public String refreshStock(@PathVariable String productId) throws SellException {
		SecProductInfo secProductInfo = secKillService.refreshStock(productId);
		return "库存id为 "+productId +" <br>  库存总量为 "+secProductInfo.getStock();
	}
}
