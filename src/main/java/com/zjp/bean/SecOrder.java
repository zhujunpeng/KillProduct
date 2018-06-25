package com.zjp.bean;

import java.io.Serializable;
import java.math.BigDecimal;

import com.zjp.util.KeyUtil;

public class SecOrder implements Serializable{
	
	private static final long serialVersionUID = 2455321305223045449L;
	private String id;
	private String userId;
	// 商品id
	private String productId;
	// 商品价格
	private BigDecimal  productPrice;
	private BigDecimal  amount;
	
	public SecOrder(String productId) {
		String utilId = KeyUtil.getUniqueKey();
		this.id = utilId;
		this.userId = "userId" + utilId;
		this.productId = productId;
	}

	public SecOrder() {}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public BigDecimal getProductPrice() {
		return productPrice;
	}
	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}
