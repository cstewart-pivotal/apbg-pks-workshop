package io.pivotal.pcf.demo.fortunebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableAutoConfiguration
@EnableRedisRepositories
public class FortuneBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FortuneBackendApplication.class, args);
	}
}
