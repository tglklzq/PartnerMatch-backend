package org.lzq.partnermatchbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("org.lzq.partnermatchbackend.mapper")
@SpringBootApplication
@EnableScheduling
public class PartnerMatchBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PartnerMatchBackendApplication.class, args);
	}

}
