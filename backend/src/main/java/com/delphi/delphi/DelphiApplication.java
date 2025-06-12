package com.delphi.delphi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DelphiApplication {

	@Value("${repository-gen-sys-prompt}")
	private String repositoryGenSysPrompt;

	public static void main(String[] args) {
		SpringApplication.run(DelphiApplication.class, args);
	}

}
