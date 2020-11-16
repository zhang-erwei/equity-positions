package com.erwei.ep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan("com.erwei.ep.mapper")
@SpringBootApplication
//@EnableScheduling//开启定时任务
public class EquityPositionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(EquityPositionsApplication.class, args);
	}

}
