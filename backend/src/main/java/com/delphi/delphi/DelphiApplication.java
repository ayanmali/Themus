package com.delphi.delphi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
// TODO: Enable async support?
// @EnableAsync

public class DelphiApplication {
	// TODO: add authorization to all endpoints
	// TODO: add more endpoints for data access and query params for get requests
	@GetMapping("/")
	public String hello() {
		return "Hello, World!";
	}

	@GetMapping("/welcome")
	public String welcome() {
		return "Welcome";
	}

	public static void main(String[] args) {
		SpringApplication.run(DelphiApplication.class, args);
	}

}
