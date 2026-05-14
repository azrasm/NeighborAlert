package com.projekat.report_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.modelmapper.ModelMapper;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ReportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportServiceApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
    	return new ModelMapper();
	}

	@Bean(name = "multipartResolver")
	public StandardServletMultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver();
	}
}
