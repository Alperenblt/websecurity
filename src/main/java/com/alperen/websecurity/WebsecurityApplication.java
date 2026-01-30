package com.alperen.websecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebsecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsecurityApplication.class, args);
	}

}
