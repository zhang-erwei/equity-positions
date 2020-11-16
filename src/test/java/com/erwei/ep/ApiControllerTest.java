package com.erwei.ep;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.erwei.ep.api.TransactionException;
import com.erwei.ep.entity.EquityPosition;
import com.erwei.ep.entity.Trade;
import com.erwei.ep.entity.Transaction;
import com.erwei.ep.web.AjaxResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ApiControllerTest {

	protected MockMvc mvc;
	private MockHttpSession session;

	@Autowired
	protected WebApplicationContext wac;

	@Before
	public void setupMockMvc() {
		mvc = MockMvcBuilders.webAppContextSetup(wac).build(); // 初始化MockMvc对象
		session = new MockHttpSession();
//		ThreadContext.put(UserService.LOGIN_USER, user);
	}

	@Test
	public void processTransaction() throws Exception {
		Set<Transaction> sets = new HashSet<Transaction>();
		sets.add(new Transaction("1", "1", 1, "REL", 50, "INSERT", Trade.TYPE_BUY));
		sets.add(new Transaction("2", "2", 1, "ITC", 40, "INSERT", Trade.TYPE_SELL));
		sets.add(new Transaction("3", "3", 1, "INF", 70, "INSERT", Trade.TYPE_BUY));
		sets.add(new Transaction("4", "1", 2, "REL", 60, "UPDATE", Trade.TYPE_BUY));
		sets.add(new Transaction("5", "2", 2, "ITC", 30, "CANCEL", Trade.TYPE_BUY));
		sets.add(new Transaction("6", "4", 1, "INF", 20, "INSERT", Trade.TYPE_SELL));

		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(5));
		for (Transaction trans : sets) {
			executor.execute(new Runnable() {
				public void run() {
					log.info("正在处理：TansactionId= {}", trans.getId());
					try {
						processTransaction(trans);
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

		this.listTransactions();
		String json = this.listPositions();
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		List<EquityPosition> list = mapper.readValue(json, new TypeReference<AjaxResult<List<EquityPosition>>>() {
		}).getData();
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
	public void testAll() throws Exception {
		String trade1 = this.buy("REL", 50);
		String trade2 = this.buy("ITC", 40);
		String trade3 = this.sell("ITC", 40);
		this.buy("INF", 70);
		this.update(trade1, "REL", 60, Trade.TYPE_BUY);
		this.cancel(trade3);
		this.cancel(trade2);
		this.sell("INF", 20);

		this.listTransactions();
		String json = this.listPositions();
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		List<EquityPosition> list = mapper.readValue(json, new TypeReference<AjaxResult<List<EquityPosition>>>() {
		}).getData();
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

	private AjaxResult<Trade> toAjaxResult(String json) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		return mapper.readValue(json, new TypeReference<AjaxResult<Trade>>() {
		});
	}

	private String buy(String securityCode, int quantity) throws Exception {
		String result = mvc.perform(MockMvcRequestBuilders.post("/api/buy")
//              .content(json.getBytes()) //传json参数
				.param("securityCode", securityCode).param("quantity", quantity + "").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("code").value(0))
//    	.andDo(MockMvcResultHandlers.print())
				.andReturn().getResponse().getContentAsString();
		return toAjaxResult(result).getData().getId();
	}

	private String sell(String securityCode, int quantity) throws Exception {
		String result = mvc.perform(MockMvcRequestBuilders.post("/api/sell")
//              .content(json.getBytes()) //传json参数
				.param("securityCode", securityCode).param("quantity", quantity + "").session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("code").value(0))
//    	.andDo(MockMvcResultHandlers.print())
				.andReturn().getResponse().getContentAsString();
		return toAjaxResult(result).getData().getId();

	}

	private void cancel(String tradeId) throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/api/cancel").accept(MediaType.APPLICATION_JSON_UTF8)
//              .content(json.getBytes()) //传json参数
				.param("tradeId", tradeId).session(session)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("code").value(0))
//    	.andDo(MockMvcResultHandlers.print())
				.andReturn().getResponse().getContentAsString();
	}

	private void update(String tradeId, String securityCode, int quantity, int tradeType) throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/api/update").accept(MediaType.APPLICATION_JSON_UTF8)
//              .content(json.getBytes()) //传json参数
				.param("tradeId", tradeId).param("securityCode", securityCode).param("quantity", quantity + "")
				.param("tradeType", tradeType + "").session(session)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("code").value(0))
//    	.andDo(MockMvcResultHandlers.print())
				.andReturn().getResponse().getContentAsString();
	}

	private void processTransaction(Transaction trans) throws TransactionException {
		try {
			mvc.perform(MockMvcRequestBuilders.post("/api/processTransaction").accept(MediaType.APPLICATION_JSON_UTF8)
//              .content(json.getBytes()) //传json参数
					.param("id", trans.getId()).param("tradeId", trans.getTradeId())
					.param("version", trans.getVersion() + "").param("securityCode", trans.getSecurityCode())
					.param("quantity", trans.getQuantity() + "").param("transactionType", trans.getTransactionType())
					.param("tradeType", trans.getTradeType() + "").session(session))
					.andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(MockMvcResultMatchers.jsonPath("code").value(0));
//    	.andDo(MockMvcResultHandlers.print())
//			.andReturn().getResponse().getContentAsString();
		} catch (Throwable e) {
			throw new TransactionException(2003, e.getMessage(), trans);

		}
	}

	private void listTransactions() throws Exception {
		String json = mvc
				.perform(MockMvcRequestBuilders.post("/api/listTransactions").accept(MediaType.APPLICATION_JSON_UTF8)
//              .content(json.getBytes()) //传json参数
						.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("code").value(0))
//    	.andDo(MockMvcResultHandlers.print())
				.andReturn().getResponse().getContentAsString();

		JSONObject.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";// 设置日期格式
		JSONObject object = JSONObject.parseObject(json);
		String pretty = JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
				SerializerFeature.WriteDateUseDateFormat);
		log.info("===================Transactions:===========");
		log.info(pretty);
	}

	private String listPositions() throws Exception {
		String json = mvc
				.perform(MockMvcRequestBuilders.post("/api/listPositions").accept(MediaType.APPLICATION_JSON_UTF8)
//              .content(json.getBytes()) //传json参数
						.session(session))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("code").value(0))
//    	.andDo(MockMvcResultHandlers.print())
				.andReturn().getResponse().getContentAsString();
		JSONObject.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";// 设置日期格式
		JSONObject object = JSONObject.parseObject(json);
		String pretty = JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
				SerializerFeature.WriteDateUseDateFormat);
		log.info("===================Points:===========");
		log.info(pretty);
		return json;
	}

}
