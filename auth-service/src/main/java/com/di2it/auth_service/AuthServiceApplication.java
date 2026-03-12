package com.di2it.auth_service;

import com.di2it.auth_service.config.CookieProperties;
import com.di2it.auth_service.config.JwtKeyProperties;
import com.di2it.auth_service.config.MfaProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    JwtKeyProperties.class,
    MfaProperties.class,
    CookieProperties.class
})
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
