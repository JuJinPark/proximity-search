package com.example.proximity_search;

import com.example.proximity_search.benchmark.ProximitySearchBenchmark;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProximitySearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProximitySearchApplication.class, args);
	}

	@Bean
	public CommandLineRunner runBenchmark(ProximitySearchBenchmark benchmark) {
		return args -> {
			benchmark.runBenchmark();
			System.exit(0);
		};
	}

}
