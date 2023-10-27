package com.example.demoSpringIntegration;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@SpringBootApplication
public class DemoSpringIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoSpringIntegrationApplication.class, args);
	}
	
	@Component
	static class MyMessageSource implements MessageSource<String> {
		private static String text(){
			return Math.random() > .5 ?
					"Hello world @ " + Instant.now() :
					"Hola @ " + Instant.now();
		}
		
		@Override
		public Message<String> receive() {
			return MessageBuilder.withPayload(text()).build();
		}
	}
	
	@Bean
	ApplicationRunner runner(MyMessageSource myMessageSource, IntegrationFlowContext integrationFlowContext){
		return	args ->{
			var holaFlow = flow(myMessageSource, 1000, "hola");
			var helloFlow = flow(myMessageSource, 3000, "hello");
			Set.of(helloFlow, holaFlow).forEach(flow -> integrationFlowContext.registration(flow).register().start());
		};
	}
	
	private static IntegrationFlow flow(MyMessageSource myMessageSource, int seconds, String filterText){
		return IntegrationFlow
				.from(myMessageSource, 
						sourcePollingChannelAdapterSpec -> 
								sourcePollingChannelAdapterSpec.poller(pollerFactory -> pollerFactory.fixedRate(seconds)))
				.filter(String.class, source -> source.contains(filterText))
				.transform((GenericTransformer<String, String>) String::toUpperCase)
				.handle((GenericHandler<String>) (payload, headers) -> {
					System.out.printf("The payload is %s%n", payload);
					return null;
				})
				.get();
	}

}
