package com.erwei.ep.mapper;

import org.apache.ibatis.annotations.Select;

import com.erwei.ep.entity.Transaction;

import tk.mybatis.mapper.common.Mapper;

public interface TransactionMapper extends Mapper<Transaction> {
	@Select("select * from t_transaction where trade_id = #{tradeId} and version = #{version}")
	public Transaction getTransaction(String tradeId,int version);
}
