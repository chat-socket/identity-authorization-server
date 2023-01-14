package com.mtvu.identityauthorizationserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author mvu
 * @project chat-socket
 **/
@SpringBootApplication
@EnableFeignClients
public class IdentityAuthorizationServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityAuthorizationServerApplication.class, args);
	}

}
