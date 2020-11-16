package com.erwei.ep.api;

import java.util.List;

import com.erwei.ep.entity.EquityPosition;
import com.erwei.ep.entity.Trade;
import com.erwei.ep.entity.Transaction;
/**
 * 交易服务接口
 * @author Administrator
 *
 */
public interface TransactionService {
	
	/**
	 * 交易处理，输入入Transaction交易对象，处理买入、卖出、更改、取消等交易，交易结果通过listPositions查看
	 * @param trans
	 */
	public void processTransaction(Transaction trans) throws TransactionException;
	
	/**
	 * 列出所有的买卖交易
	 * @return
	 */
	public List<Trade> listTrades();
	/**
	 * 查看所有交易，列出所有的买入、卖出、更改、取消等交易
	 * @return
	 */
	public List<Transaction> listTransactions();
	
	/**
	 * 查看交易结果，列出每个证券代码的证券数量。
	 * @return
	 */
	public List<EquityPosition> listPositions();
	
	
	/**
	 * 买入交易，输入证券代码和数量，完成买入交易
	 * @param securityCode
	 * @param quantity
	 * @return
	 */
	public Trade buy(String securityCode,int quantity) throws TransactionException;
	
	/**
	 * 卖出交易，输入证券代码和数量，完成卖出交易
	 * @param securityCode
	 * @param quantity
	 * @return
	 */
	public Trade sell(String securityCode,int quantity) throws TransactionException;
	
	
	/**
	 * 取消交易：输入买入或卖出交易对象的ID，取消该次买入或卖出
	 * @param tradeId
	 * @return
	 */
	public Trade cancel(String tradeId) throws TransactionException;
	
	
	/**
	 * 更改买入或卖出交易，输入买入或卖出交易对象的ID，证券代码、数量，买入或卖出类型，完成买入或卖出的更改操作
	 * @param tradeId
	 * @param securityCode
	 * @param quantity
	 * @param tradeType
	 * @return
	 * @throws TransactionException
	 */
	public Trade update(String tradeId,String securityCode,int quantity,int tradeType) throws TransactionException;
	
	
	
	
	

}
