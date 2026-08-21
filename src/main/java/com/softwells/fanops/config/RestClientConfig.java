package com.softwells.fanops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * En Spring Boot 4.0.0-M3 el auto-config de RestClient no registra RestClient.Builder,
 * así que lo definimos explícitamente (usa el request factory por defecto).
 */
@Configuration
public class RestClientConfig {

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }
}