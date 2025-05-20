package com.fpl.stats;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCaching
public class FplStatsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FplStatsApplication.class, args);
	}

}
