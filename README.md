# equity-positions

#### 实体模型设计
 系统设计了以下三个java实体对象,分别持久化到h2数据库中。

1. Trade：记录买入、卖出交易信息，
    [查看源代码](https://gitee.com/zhang_erwei/equity-positions/blob/master/src/main/java/com/erwei/ep/entity/EquityPosition.java)
2. Transaction：记录所有交易信息，操作，包括买入、卖出、更改、取消。
    [查看源代码](https://gitee.com/zhang_erwei/equity-positions/blob/master/src/main/java/com/erwei/ep/entity/Transaction.java)
3. EquityPosition：记录每次交易后证券账户数量
   [查看源代码](https://gitee.com/zhang_erwei/equity-positions/blob/master/src/main/java/com/erwei/ep/entity/EquityPosition.java)

#### 接口类设计TransactionService
 系统设计了一个交易服务接口类TransactionService，包括如下接口
 1.  交易处理，输入入Transaction交易对象，处理买入、卖出、更改、取消等交易，交易结果通过listPositions查看
  
  public void processTransaction(Transaction trans) throws TransactionException
  
 2. 查看交易结果，列出每个证券代码的证券数量。
    public List<EquityPosition> listPositions();
    
 3. 查看所有交易，列出所有的买入、卖出、更改、取消等交易
    
    public List<Transaction> listTransactions();
           
 4. 买入交易，输入证券代码和数量，完成买入交易
 
    public Trade buy(String securityCode,int quantity) throws TransactionException;
 
 5. 卖出交易，输入证券代码和数量，完成卖出交易
 
    public Trade sell(String securityCode,int quantity) throws TransactionException;
 
 6. 取消交易：输入买入或卖出交易对象的ID，取消该次买入或卖出
    public Trade cancel(String tradeId) throws TransactionException;
 
 7. 更改买入或卖出交易，输入买入或卖出交易对象的ID，证券代码、数量，买入或卖出类型，完成买入或卖出的更改操作。
    
    public Trade update(String tradeId,String securityCode,int quantity,int tradeType) throws TransactionException;
 
 [查看TransactionService源代码](https://gitee.com/zhang_erwei/equity-positions/blob/master/src/main/java/com/erwei/ep/api/TransactionService.java)


#### 接口实现类TransactionServiceImpl

 processTransaction接口实现，处理交易的流程如下：

  1. 根据Transaction对象的transactionType值可能是INSERT/UPDATE/CANCEL，分别做如下处理
  2. 如果transactionType的值是INSERT,直接根据创建Trade对象和Transaction对象，更新保存本次交易结果到EquityPosition对象
  3. 如果transactionType的值是CANCEL，这根据version的值从数据库查找相同tradeId和版本号为(version-1)的Transaction对象，
              如果没有找到，等待n秒钟再查找，直到找到上一个版本的Transaction对象后，再根据当前Transaction对象的tradeId从数据库获取Trade对象，
              根据Trade对象的买或卖操作，对对应的EquityPosition对象减去或增加相应securityCode的quantity。
  4. 如果transactionType的值是UPDATE，先根据步骤3 做CANCEL操作，然后更新Trade对象，再根据步骤2做INSERT操作。
  
   [查看TransactionServiceImpl源代码](https://gitee.com/zhang_erwei/equity-positions/blob/master/src/main/java/com/erwei/ep/impl/TransactionServiceImpl.java)


#### Web Restfull接口实现以及安全设计
类ApiController实现了TransactionService接口的所有方法，以Web Restfull的提供给其他系统调用。
  为了保证WEB接口的安全，可采取如下措施：
  
1.  提交http请求采用post方式，提高数据传输参数的安全性
2.  对外服务通过https协议，提高传输数据的安全性。参考做法之一部署ngingx服务器作转发，在nginx端作https配置。
3.  接口数据返回采用json格式,格式如下：
```json
    {
       code: 0,
       msg: 'OK',
       data:{
       }
    } 
```
  返回值code为返回码，为0时，表示本次请求成功，其他情况代表各种异常情况，可通过msg查看异常信息。
  
4.  通过用户认证与授权，实现单点登录保证安全（未在本demo实现)
  生产环境应该通过用户认证、和授权，相关权限的角色才能调用相关接口。可通过Spring boot security和Oauth2.0技术结合解决这个问题（未在本demo实现)

 [查看ApiController源代码] (https://gitee.com/zhang_erwei/equity-positions/blob/master/src/main/java/com/erwei/ep/web/ApiController.java)


#### UT for controller:ApiControllerTest
1.  processTransaction测试用例，通过线程池多线程调用processTransaction web restful接口web restful接口

```java
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
```

  2.  testAll测试用例，模拟测试了买入、卖出、更改、取消等web restful接口。
```java

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
	
```
[查看ApiControllerTest源代码](https://gitee.com/zhang_erwei/equity-positions/blob/master/src/test/java/com/erwei/ep/ApiControllerTest.java)

#### UT for interface:TransactionServiceTests
1.  processTransaction测试用例，通过线程池多线程调用processTransaction service接口
2.  testAll测试用例，模拟测试了买入、卖出、更改、取消等service接口。

[查看TransactionServiceTests源代码](https://gitee.com/zhang_erwei/equity-positions/blob/master/src/test/java/com/erwei/ep/TransactionServiceTests.java)



#### UT for failure and exception
调用processTransaction接口时，transactinType类型必须是INSERT或UPDATE或CANCEL，当输入的TransactionType不是这些值是，系统会触发异常，以下测试用例测试了异常的情况
```java
@Test
	public void testProcessTransactionException() throws TransactionException {
		Transaction trans=new Transaction("1", "1", 1, "REL", 50, "INSERT_1", Trade.TYPE_BUY);
		transService.processTransaction(trans);
	}
```
#### Log(Bonus item)
   系统运行所有日志保存日志文件中，日志文件保存10天。
   
```yml  
   logging:
  file:
    name: ./logs/equityPosition.log
    max-size: 30MB
    max-history: 10
  level:
    root: INFO
    org.hibernate.SQL: DEBUG
    #com.erwei: DEBUG
```

#### Rest API of data initialized to display the result(Bonus item)(Not UT).
系统以spring boot项目在本机启动后，访问
```http
http://localhost:8080/init
```
系统自动初始化数据处理所有交易数据，跳转到
```http
http://localhost:8080/index
```
    并展示处理结果,
 [查看界面展示效果：https://gitee.com/zhang_erwei/equity-positions/blob/master/index.png](https://gitee.com/zhang_erwei/equity-positions/blob/master/index.png)

####  Spring boot project (configuration of .yml or .properties)

[查看spring boot yml配置文件] (https://gitee.com/zhang_erwei/equity-positions/blob/master/src/main/resources/application.yml)

#### Maven project (pom.xml);

[查看pom文件](https://gitee.com/zhang_erwei/equity-positions/blob/master/pom.xml)