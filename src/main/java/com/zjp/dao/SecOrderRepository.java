package com.zjp.dao;

import java.util.List;

import com.zjp.bean.SecOrder;

public interface SecOrderRepository {

	List<SecOrder> findByProductId(String productId);

	SecOrder save(SecOrder secOrder);
}
