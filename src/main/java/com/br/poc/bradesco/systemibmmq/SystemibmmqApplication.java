package com.br.poc.bradesco.systemibmmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class SystemibmmqApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemibmmqApplication.class, args);
	}

}
