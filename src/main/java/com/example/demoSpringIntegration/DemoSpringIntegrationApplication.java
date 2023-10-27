package com.example.demoSpringIntegration;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;

@SpringBootApplication
public class DemoSpringIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoSpringIntegrationApplication.class, args);
	}

	@Bean
	MessageChannel greetings(){
		return MessageChannels.direct().getObject();
	}

	private String text(){
		return Math.random() > .5 ?
				"Hello world @ " + Instant.now() :
				"Hola @ " + Instant.now();
	}

	@Bean
	ApplicationRunner runner(){
		return args -> {
			for (int i = 0; i < 10; i++) {
				greetings().send(MessageBuilder.withPayload(text()).build());
			}
		};
	}

	@Bean
	IntegrationFlow flow(){
		return IntegrationFlow
				.from(greetings())
				.filter(String.class, source -> source.contains("Hola"))
				.transform((GenericTransformer<String, String>) String::toUpperCase)
				.handle((GenericHandler<String>) (payload, headers) -> {
					System.out.printf("The payload is %s%n", payload);
					return null;
				})
				.get();
	}

}
