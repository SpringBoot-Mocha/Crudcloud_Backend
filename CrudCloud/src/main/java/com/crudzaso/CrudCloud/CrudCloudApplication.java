package com.crudzaso.CrudCloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.crudzaso.CrudCloud.repository")
public class CrudCloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrudCloudApplication.class, args);
	}

}
