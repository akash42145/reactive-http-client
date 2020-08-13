package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ReactiveHttpClientApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ReactiveHttpClientApplication.class, args);
	}
	
	@Bean
	WebClient webClient(WebClient.Builder builder) {
		return builder.baseUrl("http://localhost:8080")
//		.filter(ExchangeFilterFunctions.basicAuthentication())
		.build();
	}

}

@Component
@RequiredArgsConstructor
@Log4j2
class client {
	private final WebClient webClient;
	
	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		var name = "Spring fans..";
		
		this.webClient.get()
		.uri("/wishing/{name}", name)
		.retrieve()
		.bodyToMono(GreetingResponse.class)
		.map(GreetingResponse::getMessage)
		.onErrorMap(throwable -> new IllegalArgumentException("Original exception was "+ throwable.toString()))
		.onErrorResume(IllegalArgumentException.class, ex -> Mono.just(ex.toString()))
		.subscribe(m -> log.info("Mono:: "+m));
		
		this.webClient.get().uri("/wishings/{name}", name)
		.retrieve()
		.bodyToFlux(GreetingResponse.class)
		.subscribe(l -> log.info("FLUX:: "+l.getMessage()));
	}
	
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingResponse{
	private String message;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingRequest{
	private String name;
}

