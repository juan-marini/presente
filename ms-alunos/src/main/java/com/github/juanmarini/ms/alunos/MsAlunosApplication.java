package com.github.juanmarini.ms.alunos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsAlunosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAlunosApplication.class, args);
	}

}
