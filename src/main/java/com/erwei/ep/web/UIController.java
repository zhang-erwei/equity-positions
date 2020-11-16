package com.erwei.ep.web;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.erwei.ep.api.TransactionException;
import com.erwei.ep.api.TransactionService;
import com.erwei.ep.entity.Trade;
import com.erwei.ep.entity.Transaction;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class UIController {
	@Autowired
	private TransactionService transService;
	
	@RequestMapping("/index")
	public String index(Model model) {
		model.addAttribute("trades", transService.listTrades());
		model.addAttribute("transactions", transService.listTransactions());
		model.addAttribute("positions", transService.listPositions());
		return "index";
	}
	@RequestMapping("/init")
	public String initData(Model model) throws TransactionException{
		Set<Transaction> sets = new HashSet<Transaction>();
		sets.add(new Transaction("1", "1", 1, "REL", 50, "INSERT", Trade.TYPE_BUY));
		sets.add(new Transaction("2", "2", 1, "ITC", 40, "INSERT", Trade.TYPE_SELL));
		sets.add(new Transaction("3", "3", 1, "INF", 70, "INSERT", Trade.TYPE_BUY));
		sets.add(new Transaction("4", "1", 2, "REL", 60, "UPDATE", Trade.TYPE_BUY));
		sets.add(new Transaction("5", "2", 2, "ITC", 30, "CANCEL", Trade.TYPE_BUY));
		sets.add(new Transaction("6", "4", 1, "INF", 20, "INSERT", Trade.TYPE_SELL));

		ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 10, 200, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(5));
		for (Transaction trans : sets) {
			executor.execute(new Runnable() {
				public void run() {
					log.info("正在处理：TansactionId= {}", trans.getId());
					try {
						transService.processTransaction(trans);
						log.info("处理完毕：TansactionId= {}", trans.getId());
					} catch (TransactionException e) {
						log.info("发生错误：TansactionId= {}", trans.getId());
						e.printStackTrace();

					}

				}
			});

		}
		while (true) {
			if (executor.getCompletedTaskCount() == 6) {
				executor.shutdown();
				break;
			}
			try {
				Thread.sleep(2000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	return "redirect:/index";
	}
	
	@RequestMapping("/init2")
	public String initData2(Model model) throws TransactionException{
//		try {
			Trade trade1=transService.buy("REL", 50);
			Trade trade2=transService.buy("ITC", 40);
			Trade trade3=transService.sell("ITC", 40);
			transService.buy("INF", 70);
			transService.update(trade1.getId(), "REL", 60, Trade.TYPE_BUY);
			transService.cancel(trade3.getId());
			transService.cancel(trade2.getId());
			transService.sell("INF", 20);
//		} catch (TransactionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	return "redirect:/index";
	}

	
	@PostMapping("/buy")
	public String buy(String securityCode,int quantity) throws TransactionException{
		transService.buy(securityCode, quantity);
		return "redirect:/index";
	}
	
	@PostMapping("/sell")
	public String sell(String securityCode,int quantity) throws TransactionException{
		transService.sell(securityCode, quantity);
		return "redirect:/index";
	}
	
	@PostMapping("/cancel")
	public String cancel(String tradeId) throws TransactionException{
		transService.cancel(tradeId);
		return "redirect:/index";
	}
	
	@PostMapping("/update")
	public String update(String tradeId,String securityCode,int quantity,int tradeType) throws TransactionException{
		transService.update(tradeId, securityCode, quantity, tradeType);
		return "redirect:/index";
		
	}
}
