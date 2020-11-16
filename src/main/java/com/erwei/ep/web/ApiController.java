package com.erwei.ep.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erwei.ep.api.TransactionException;
import com.erwei.ep.api.TransactionService;
import com.erwei.ep.entity.Trade;
import com.erwei.ep.entity.Transaction;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Validated
@RequestMapping("/api")
public class ApiController {
	@Autowired
	private TransactionService transService;
	
	@PostMapping("/buy")
	public AjaxResult buy(String securityCode,int quantity) {
		try {
			Trade trade=transService.buy(securityCode, quantity);
			return AjaxResult.success(trade);
		} catch (TransactionException e1) {
			log.error(e1.getMessage());
			return AjaxResult.error(e1);
		}catch (Throwable e) {
			log.error("交易失败",e);
			return AjaxResult.error(e.getMessage());
		}
	}
	
	@PostMapping("/sell")
	public AjaxResult sell(String securityCode,int quantity) {
		try {
			Trade trade=transService.sell(securityCode, quantity);
			return AjaxResult.success(trade);
		} catch (TransactionException e1) {
			log.error(e1.getMessage());
			return AjaxResult.error(e1);
		}catch (Throwable e) {
			log.error("交易失败",e);
			return AjaxResult.error(e.getMessage());
		}
	}
	
	@PostMapping("/cancel")
	public AjaxResult cancel(String tradeId) {
		try {
			Trade trade=transService.cancel(tradeId);
			return AjaxResult.success(trade);
		} catch (TransactionException e1) {
			log.error(e1.getMessage());
			return AjaxResult.error(e1);
		}catch (Throwable e) {
			log.error("交易失败",e);
			return AjaxResult.error(e.getMessage());
		}
	}
	
	@PostMapping("/update")
	public AjaxResult update(String tradeId,String securityCode,int quantity,int tradeType) {
		try {
			Trade trade=transService.update(tradeId, securityCode, quantity, tradeType);
			return AjaxResult.success(trade);
		} catch (TransactionException e1) {
			log.error(e1.getMessage());
			return AjaxResult.error(e1);
		}catch (Throwable e) {
			log.error("交易失败",e);
			return AjaxResult.error(e.getMessage());
		}
	}
	
	@PostMapping("/processTransaction")
	public AjaxResult processTransaction(Transaction trans) {
		try {
			transService.processTransaction(trans);
			return AjaxResult.success("OK");
		} catch (TransactionException e1) {
			log.error(e1.getMessage());
			return AjaxResult.error(e1);
		}catch (Throwable e) {
			log.error("交易失败",e);
			return AjaxResult.error(e.getMessage());
		}
	}
	
	@RequestMapping("/listTransactions")
	public AjaxResult listTransactions() {
		try {
			return AjaxResult.success(transService.listTransactions());
		} catch (Throwable e) {
			log.error("交易失败",e);
			return AjaxResult.error(e.getMessage());
		}
	}
	
	@RequestMapping("/listPositions")
	public AjaxResult listPositions() {
		try {
			return AjaxResult.success(transService.listPositions());
		} catch (Throwable e) {
			log.error("交易失败",e);
			return AjaxResult.error(e.getMessage());
		}
	}

}
