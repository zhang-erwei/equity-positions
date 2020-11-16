package com.erwei.ep.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 记录买入、卖出交易信息
 * 
 * @author zhang。erwei@163.com
 *
 */
@Entity
@Table(name = "t_trade")
@Data
public class Trade {
	public Trade() {
		this.id=UUID.randomUUID().toString().toUpperCase().replace("-", "");
		this.createTime=new Date();
	}
	public static final Integer TYPE_BUY=1;
	public static final Integer TYPE_SELL=2;
//	public static final String TYPE_BUY="Buy";
//	public static final String TYPE_SELL="Sell";
	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String id;
	
	/**
	 * 证券代码
	 */
	private String securityCode;
	
	/**
	 * 交易数量
	 */
	private Integer quantity;
	
	/**
	 * 交易类型:  1:买；2：卖
	 */
	private Integer type;
	
	/**
	 *  买卖交易是否已取消:  1:买；2：卖
	 */
	private Boolean cancel=false;
	
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	private Date updateTime;
}
