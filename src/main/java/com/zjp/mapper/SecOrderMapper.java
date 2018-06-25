package com.zjp.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.zjp.bean.SecOrder;

@Repository
public interface SecOrderMapper {

	List<SecOrder> findByProductId(String productId);

    SecOrder save(SecOrder secOrder);
}
