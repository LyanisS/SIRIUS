package fr.episen.sirius.pcc.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PCCBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(PCCBackApplication.class, args);
	}
}
