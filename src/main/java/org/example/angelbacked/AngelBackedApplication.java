package org.example.angelbacked;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.angelbacked.mapper")
public class AngelBackedApplication {

	public static void main(String[] args) {
		SpringApplication.run(AngelBackedApplication.class, args);
	}

}