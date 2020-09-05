package com.dub.gutenberg;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import com.dub.gutenberg.config.ElasticsearchConfig;

@SpringBootApplication
@EnableDiscoveryClient
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	private ElasticsearchConfig elasticsearchConfig;
	
	@Value("${elasticsearch-host}")
	private String elasticsearchHost;
	
	@Bean
	@DependsOn("elasticsearchConfig")
	public RestHighLevelClient client() {
		RestHighLevelClient client = new RestHighLevelClient(
	                RestClient.builder(
	                        new HttpHost(
	                        		elasticsearchConfig.getHost(),
	                        		elasticsearchConfig.getPortOne(),
	                        		elasticsearchConfig.getScheme()),
	                        new HttpHost(elasticsearchConfig.getHost(),
	                        		elasticsearchConfig.getPortTwo(),
	                        		elasticsearchConfig.getScheme())
	                )
		);
		
		return client;
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
