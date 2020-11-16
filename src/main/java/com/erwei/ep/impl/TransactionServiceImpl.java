package com.erwei.ep.impl;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erwei.ep.api.TransactionException;
import com.erwei.ep.api.TransactionService;
import com.erwei.ep.entity.EquityPosition;
import com.erwei.ep.entity.Trade;
import com.erwei.ep.entity.Transaction;
import com.erwei.ep.mapper.EquityPositionMapper;
import com.erwei.ep.mapper.TradeMapper;
import com.erwei.ep.mapper.TransactionMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单服务
 */
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private TradeMapper tradeMapper;

	@Autowired
	private TransactionMapper transactionMapper;

	@Autowired
	private EquityPositionMapper equityPositionMapper;

	/**
	 * 计算position
	 * 
	 * @param position
	 * @param trans
	 * @return
	 */
	private EquityPosition calculate(Transaction trans) throws TransactionException {
		EquityPosition position = equityPositionMapper.selectByPrimaryKey(trans.getSecurityCode());
		if (position == null) {
			position = new EquityPosition();
			position.setSecurityCode(trans.getSecurityCode());
			position.setQuantity(0);
			equityPositionMapper.insert(position);
		}

		if (Transaction.TRANSACTION_TYPE_INSERT.equals(trans.getTransactionType())) {
			if (trans.getTradeType().intValue() == Trade.TYPE_BUY) {
				position.setQuantity(position.getQuantity().intValue() + trans.getQuantity().intValue());
			} else if (trans.getTradeType().intValue() == Trade.TYPE_SELL) {
				position.setQuantity(position.getQuantity().intValue() - trans.getQuantity().intValue());
			}
		} else if (Transaction.TRANSACTION_TYPE_UPDATE.equals(trans.getTransactionType())) {
			if (trans.getTradeType().intValue() == Trade.TYPE_BUY) {
				position.setQuantity(position.getQuantity().intValue() + trans.getQuantity().intValue());
			} else if (trans.getTradeType().intValue() == Trade.TYPE_SELL) {
				position.setQuantity(position.getQuantity().intValue() - trans.getQuantity().intValue());
			}
		} else if (Transaction.TRANSACTION_TYPE_CANCEL.equals(trans.getTransactionType())) {
			if (trans.getTradeType().intValue() == Trade.TYPE_BUY) {
				position.setQuantity(position.getQuantity().intValue() - trans.getQuantity().intValue());
			} else if (trans.getTradeType().intValue() == Trade.TYPE_SELL) {
				position.setQuantity(position.getQuantity().intValue() + trans.getQuantity().intValue());
			}
		}
//		if (position.getQuantity().intValue() < 0) {
//			throw new TransactionException(TransactionException.INVALID_PARAM, "证券剩余数量不足，无法完成交易", trans);
//		}

		return position;
	}

	@Override
	@Transactional
	public Trade buy(String securityCode, int quantity) throws TransactionException {
		Trade trade = new Trade();
		trade.setType(Trade.TYPE_BUY);
		trade.setSecurityCode(securityCode);
		trade.setQuantity(quantity);
		tradeMapper.insert(trade);

		Transaction trans = new Transaction();
		trans.setTradeId(trade.getId());
		trans.setVersion(1);
		trans.setTradeType(Trade.TYPE_BUY);
		trans.setTransactionType("INSERT");
		trans.setSecurityCode(securityCode);
		trans.setQuantity(quantity);
		transactionMapper.insert(trans);

		// 计算position,并保持
				EquityPosition position = this.calculate(trans);
				equityPositionMapper.updateByPrimaryKey(position);

		return trade;
	}

	@Override
	@Transactional
	public Trade sell(String securityCode, int quantity) throws TransactionException {
		Trade trade = new Trade();
		trade.setType(Trade.TYPE_SELL);
		trade.setSecurityCode(securityCode);
		trade.setQuantity(quantity);
		tradeMapper.insert(trade);

		Transaction trans = new Transaction();
		trans.setTradeId(trade.getId());
		trans.setVersion(1);
		trans.setTradeType(Trade.TYPE_SELL);
		trans.setTransactionType("INSERT");
		trans.setSecurityCode(securityCode);
		trans.setQuantity(quantity);
		transactionMapper.insert(trans);

		// 计算position,并保持
		EquityPosition position = this.calculate(trans);
		equityPositionMapper.updateByPrimaryKey(position);

		return trade;
	}

	@Override
	@Transactional
	public Trade cancel(String tradeId) throws TransactionException {
		Trade trade = tradeMapper.selectByPrimaryKey(tradeId);
		if (trade == null) {
			throw new RuntimeException("交易对象不存在");
		}

		Transaction trans = new Transaction();
		trans.setTradeId(trade.getId());
		trans.setVersion(2);
		trans.setTradeType(trade.getType());
		trans.setTransactionType("CANCEL");
		trans.setSecurityCode(trade.getSecurityCode());
		trans.setQuantity(trade.getQuantity());
		transactionMapper.insert(trans);

		// 计算position,并保持
		EquityPosition position = this.calculate(trans);
		equityPositionMapper.updateByPrimaryKey(position);
		
		trade.setCancel(true);
		trade.setUpdateTime(new Date());
		tradeMapper.updateByPrimaryKey(trade);
		return trade;
	}

	@Override
	@Transactional
	public Trade update(String tradeId, String securityCode, int quantity, int tradeType) throws TransactionException {
		Trade trade = tradeMapper.selectByPrimaryKey(tradeId);
		if (trade == null) {
			throw new RuntimeException("交易对象不存在");
		}

		// first cancel the trade
		Transaction trans = new Transaction();
		trans.setTradeId(trade.getId());
		trans.setVersion(2);
		trans.setTradeType(trade.getType());
		trans.setTransactionType("CANCEL");
		trans.setSecurityCode(trade.getSecurityCode());
		trans.setQuantity(trade.getQuantity());
		// 计算position,并保持
				EquityPosition position = this.calculate(trans);
				equityPositionMapper.updateByPrimaryKey(position);

		trans = new Transaction();
		trans.setTradeId(trade.getId());
		trans.setVersion(2);
		trans.setTradeType(tradeType);
		trans.setTransactionType(Transaction.TRANSACTION_TYPE_INSERT);
		trans.setSecurityCode(securityCode);
		trans.setQuantity(quantity);
		position=this.calculate(trans);

		trans.setTransactionType(Transaction.TRANSACTION_TYPE_UPDATE);
		transactionMapper.insert(trans);
		equityPositionMapper.updateByPrimaryKey(position);

		// update trade
		trade.setSecurityCode(securityCode);
		trade.setQuantity(quantity);
		trade.setType(tradeType);		
		trade.setUpdateTime(new Date());
		tradeMapper.updateByPrimaryKey(trade);
		return trade;

	}

	@Override
	public void processTransaction(Transaction trans) throws TransactionException {		
		//Trade trade=tradeMapper.selectByPrimaryKey(trans.getTradeId());
		if(trans.getTransactionType().equals(Transaction.TRANSACTION_TYPE_INSERT)) {
			Trade trade=new Trade();
			trade.setId(trans.getTradeId());
			trade.setSecurityCode(trans.getSecurityCode());
			trade.setQuantity(trans.getQuantity());
			trade.setType(trans.getTradeType());
			tradeMapper.insert(trade);
			transactionMapper.insert(trans);
			EquityPosition position = this.calculate(trans);
			equityPositionMapper.updateByPrimaryKey(position);			
		}else if(trans.getTransactionType().equals(Transaction.TRANSACTION_TYPE_CANCEL)||trans.getTransactionType().equals(Transaction.TRANSACTION_TYPE_UPDATE)){
			int preVersion=trans.getVersion()-1;
			while(true) {
				Transaction preTrans= transactionMapper.getTransaction(trans.getTradeId(),preVersion);
				if(preTrans==null) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					continue;
				}else {
					Trade trade=tradeMapper.selectByPrimaryKey(trans.getTradeId());
					if(trans.getTransactionType().equals(Transaction.TRANSACTION_TYPE_UPDATE)){	
						// first cancel the trade
						Transaction trans2 = new Transaction();
						trans2.setTradeId(trade.getId());
						trans2.setVersion(2);
						trans2.setTradeType(trade.getType());
						trans2.setTransactionType("CANCEL");
						trans2.setSecurityCode(trade.getSecurityCode());
						trans2.setQuantity(trade.getQuantity());
						// 计算position,并保持
						EquityPosition position = this.calculate(trans2);
						equityPositionMapper.updateByPrimaryKey(position);
								
						trade.setSecurityCode(trans.getSecurityCode());
						trade.setQuantity(trans.getQuantity());
						trade.setType(trans.getTradeType());
						trade.setUpdateTime(new Date());	
						
						tradeMapper.updateByPrimaryKey(trade);
						transactionMapper.insert(trans);
						position = this.calculate(trans);
						equityPositionMapper.updateByPrimaryKey(position);
						
					}else if(trans.getTransactionType().equals(Transaction.TRANSACTION_TYPE_CANCEL)){
						trade.setUpdateTime(new Date());
						trade.setCancel(true);
						tradeMapper.updateByPrimaryKey(trade);
						transactionMapper.insert(trans);
						trans.setQuantity(trade.getQuantity());
						trans.setTradeType(trade.getType());
						EquityPosition position = this.calculate(trans);
						equityPositionMapper.updateByPrimaryKey(position);
					}
					
					break;
				}
			}
			
		}else if(trans.getTransactionType().equals(Transaction.TRANSACTION_TYPE_UPDATE)){
			while(true) {
				Trade trade=tradeMapper.selectByPrimaryKey(trans.getTradeId());
				if(trade==null) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					continue;
				}else {
					EquityPosition position = this.calculate(trans);
					equityPositionMapper.updateByPrimaryKey(position);
					break;
				}
			}
		}else {
			throw new TransactionException(TransactionException.INVALID_PARAM,"transactinType必须是INSERT或UPDATE或CANCEL",trans);
		}
		
	}

	@Override
	public List<Trade> listTrades() {
		return tradeMapper.selectAll();
	}

	@Override
	public List<Transaction> listTransactions() {
		return transactionMapper.selectAll();
	}

	@Override
	public List<EquityPosition> listPositions() {
		return equityPositionMapper.selectAll();
	}

}
