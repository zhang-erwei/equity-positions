package com.erwei.ep.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
/**
 * 记录每次交易后证券账户数量
 * 
 * @author zhang。erwei@163.com
 *
 */
@Entity
@Table(name = "t_equity_position")
@Data
public class EquityPosition {	
	@Id
	private String securityCode;
	
	
	private Integer quantity;
}
