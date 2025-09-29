package com.memozy.memozy_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MemozyBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemozyBackApplication.class, args);
	}

}
