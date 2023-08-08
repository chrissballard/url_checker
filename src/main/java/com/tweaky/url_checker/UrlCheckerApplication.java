package com.tweaky.url_checker;

import java.util.AbstractMap.SimpleEntry;
import java.time.Duration;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class UrlCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrlCheckerApplication.class, args);

		List<String> urls = List.of("https://google.com", "https://github.com"); // Your URLs

		WebClient webClient = WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(
						reactor.netty.http.client.HttpClient.create().responseTimeout(Duration.ofSeconds(2)))) // Set 2-second timeout
				.build();

		Flux.fromIterable(urls)
				.flatMap(url -> webClient.get().uri(url)
						.exchangeToMono(response -> Mono.just(new SimpleEntry<>(url, response.statusCode())))
						.onErrorResume(e -> Mono.just(new SimpleEntry<>(url, HttpStatus.REQUEST_TIMEOUT)))) // Handle timeouts
				.subscribe(entry -> log.info("URL: " + entry.getKey() + " Status: " + entry.getValue()));
	}
}
