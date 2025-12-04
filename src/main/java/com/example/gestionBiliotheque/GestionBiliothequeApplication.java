package com.example.gestionBiliotheque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionBiliothequeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionBiliothequeApplication.class, args);
	}

}
