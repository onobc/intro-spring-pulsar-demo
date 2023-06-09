package com.acme.pulsar;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.acme.pulsar.PulsarApplication.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

@Service
public class UserService {

	private final ObjectMapper objectMapper;

	public UserService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	User singleUser() {
		return singleUserAsync().join();
	}

	Flux<User> multipleUsers() {
		return Mono.fromFuture(this.singleUserAsync())
				.delaySubscription(Duration.ofSeconds(1))
				.repeat();
	}

	private CompletableFuture<User> singleUserAsync() {
		var request = HttpRequest
				.newBuilder(URI.create("https://random-data-api.com/api/v2/users"))
				.build();
		return HttpClient.newHttpClient()
				.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(this::convertResponseBodyToUser);
	}

	private User convertResponseBodyToUser(HttpResponse<String> response) {
		try {
			return objectMapper.readValue(response.body(), User.class);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
