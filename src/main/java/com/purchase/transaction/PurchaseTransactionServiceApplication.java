package com.purchase.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableWebMvc
@EnableSwagger2
@EnableScheduling
@EnableFeignClients
@SpringBootApplication
public class PurchaseTransactionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PurchaseTransactionServiceApplication.class, args);
	}

}
