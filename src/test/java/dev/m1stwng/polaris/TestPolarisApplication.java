package dev.m1stwng.polaris;

import org.springframework.boot.SpringApplication;

public class TestPolarisApplication {

	public static void main(String[] args) {
		SpringApplication.from(PolarisApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
