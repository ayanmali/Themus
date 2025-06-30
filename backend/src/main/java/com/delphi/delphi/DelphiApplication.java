package com.delphi.delphi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
// TODO: Enable async support?
// @EnableAsync
public class DelphiApplication {
// TODO: use security context
	public static void main(String[] args) {
		SpringApplication.run(DelphiApplication.class, args);
	}

}
