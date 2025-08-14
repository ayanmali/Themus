package com.delphi.delphi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.dtos.cache.UserCacheDto;
import com.delphi.delphi.services.UserService;

@SpringBootApplication
@EnableScheduling
@RestController
// TODO: Enable async support?
// @EnableAsync

public class ThemusApplication {

    private final UserService userService;

    ThemusApplication(UserService userService) {
        this.userService = userService;
    }
	// TODO: add authorization to all endpoints
	// TODO: add more endpoints for data access
	private UserCacheDto getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
	
	@GetMapping("/")
	public String hello() {
		return "Hello, World!";
	}

	@GetMapping("/health")
	public String health() {
		UserCacheDto user = getCurrentUser();
		return "Welcome, " + user.getName() + "!";
	}

	public static void main(String[] args) {
		SpringApplication.run(ThemusApplication.class, args);
	}

}
