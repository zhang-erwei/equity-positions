package com.erwei.ep.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 记录所有交易信息，操作，包括买入、卖出、更改、取消
 * @author zhang。erwei@163.com
 *
 */
@Entity
@Table(name = "t_transaction")
@Data
public class Transaction {
	public static final String TRANSACTION_TYPE_INSERT="INSERT";
	public static final String TRANSACTION_TYPE_UPDATE="UPDATE";
	public static final String TRANSACTION_TYPE_CANCEL="CANCEL";
	
	public Transaction() {
		this.id=UUID.randomUUID().toString().toUpperCase().replace("-", "");
		this.createTime=new Date();
	}
	public Transaction(String id,String tradeId,Integer version,String securityCode,Integer quantity,String transactionType,Integer tradeType) {
		//this.id=UUID.randomUUID().toString().toUpperCase().replace("-", "");
		this.id=id;
		this.tradeId=tradeId;
		this.version=version;
		this.securityCode=securityCode;
		this.quantity=quantity;
		this.transactionType=transactionType;
		this.tradeType=tradeType;
		this.createTime=new Date();
	}
	
	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String id;
	
	private String tradeId;
	private Integer version;
	private String securityCode;
	private Integer quantity;
	/**
	 * 交易记录类型：INSERT/UPDATE/CANCEL
	 */
	private String transactionType;
	
	/**
	 *  买卖交易 类型：:  1:买；2：卖
	 */
	private Integer tradeType;
	
	
	
	/**
	 * 创建时间
	 */
	private Date createTime;
	

}
