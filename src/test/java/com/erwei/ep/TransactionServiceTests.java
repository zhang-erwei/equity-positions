package com.erwei.ep;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.erwei.ep.api.TransactionException;
import com.erwei.ep.api.TransactionService;
import com.erwei.ep.entity.EquityPosition;
import com.erwei.ep.entity.Trade;
import com.erwei.ep.entity.Transaction;
import com.erwei.ep.impl.JSON;

import lombok.extern.slf4j.Slf4j;

//import org.junit.Test;
//import org.junit.Test;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
class TransactionServiceTests {

//	protected MockMvc mvc;
//	private MockHttpSession session;
//
//	@Autowired
//	protected WebApplicationContext wac;

	@Autowired
	private TransactionService transService;

	@Test
	void contextLoads() {
	}

//	@Before
//	public void setupMockMvc() {
//		mvc = MockMvcBuilders.webAppContextSetup(wac).build(); // 初始化MockMvc对象
//		session = new MockHttpSession();
////		ThreadContext.put(UserService.LOGIN_USER, user);
//	}

	@Test
	public void testProcessTransaction() throws TransactionException {
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

		log.info("=========Input Transactions：================");
		log.info("TransactionID TradeID Version SecurityCode Quantity Insert/Update/Cancel Buy/Sell：");
		log.info(JSON.toJSON2(transService.listTransactions()));

		log.info("=========Output Position:");
		List<EquityPosition> list = transService.listPositions();
		log.info(JSON.toJSON2(list));
		for (EquityPosition pos : list) {
			// REL+60 ITC 0 INF+50
			if (pos.getSecurityCode().equals("REL")) {
				Assert.assertTrue(pos.getQuantity().intValue() == 60);
			}
			if (pos.getSecurityCode().equals("ITC")) {
				Assert.assertTrue(pos.getQuantity().intValue() == 0);
			}
			if (pos.getSecurityCode().equals("INF")) {
				Assert.assertTrue(pos.getQuantity().intValue() == 50);
			}
		}
	}
	
	@Test
	public void testProcessTransactionException() throws TransactionException {
		Transaction trans=new Transaction("1", "1", 1, "REL", 50, "INSERT_1", Trade.TYPE_BUY);
		transService.processTransaction(trans);
	}

	@Test
	public void testAll() throws TransactionException {
		Trade trade1 = transService.buy("REL", 50);
		Trade trade2 = transService.buy("ITC", 40);
		Trade trade3 = transService.sell("ITC", 40);
		transService.buy("INF", 70);
		transService.update(trade1.getId(), "REL", 60, Trade.TYPE_BUY);

		transService.cancel(trade3.getId());
		transService.cancel(trade2.getId());
		transService.sell("INF", 20);

		log.info("=========Input Transactions：================");
		log.info("TransactionID TradeID Version SecurityCode Quantity Insert/Update/Cancel Buy/Sell：");
		log.info(JSON.toJSON2(transService.listTransactions()));

		log.info("=========Output Position:");
		List<EquityPosition> list = transService.listPositions();
		log.info(JSON.toJSON2(list));
		for (EquityPosition pos : list) {
			// REL+60 ITC 0 INF+50
			if (pos.getSecurityCode().equals("REL")) {
				Assert.assertTrue(pos.getQuantity().intValue() == 60);
			}
			if (pos.getSecurityCode().equals("ITC")) {
				Assert.assertTrue(pos.getQuantity().intValue() == 0);
			}
			if (pos.getSecurityCode().equals("INF")) {
				Assert.assertTrue(pos.getQuantity().intValue() == 50);
			}

		}
	}

}
