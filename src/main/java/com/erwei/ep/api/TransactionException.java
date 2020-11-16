package com.erwei.ep.api;

import com.erwei.ep.entity.Transaction;

/**
 * 交易异常
 * @author Administrator
 *
 */
public class TransactionException extends Exception{
	/**
	 * 无效参数
	 */
	public static final Integer INVALID_PARAM=10001;
	
	private Integer code;
	private Transaction transaction;
	public TransactionException(Integer code,Transaction transaction){
		this.code=code;
		this.transaction=transaction;
	}
	
	public TransactionException(Integer code,String message,Transaction transaction){
		super(message);
		this.code=code;
		this.transaction=transaction;
	}
	public Integer getCode() {
		return this.code;
	}
	public Transaction getTransaction() {
		return this.transaction;
	}
	
	
	

}
