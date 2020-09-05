package com.dub.gutenberg;



import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.web.reactive.function.client.ExchangeStrategies;



@SpringBootApplication
@EnableDiscoveryClient
public class Application {

	@Value("${elasticsearch-host}")
	private String elasticsearchHost; 
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	 
	@Bean
	ReactiveElasticsearchClient reactiveClient() {
		
		final ClientConfiguration clientConfiguration = ClientConfiguration.builder()   
	        .connectedTo(elasticsearchHost)
	        .withWebClientConfigurer(webClient -> {                                 
	          ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
	              .codecs(configurer -> configurer.defaultCodecs()
	                  .maxInMemorySize(-1))
	              .build();
	          return webClient.mutate().exchangeStrategies(exchangeStrategies).build();
	         })
	        .build();
	
		return ReactiveRestClients.create(clientConfiguration);    
	}
	    
}
