package com.dub.gutenberg;


import java.text.DateFormat;

import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
//import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import com.dub.gutenberg.config.ElasticsearchConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import com.dub.gutenberg.config.ElasticsearchConfig;


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
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.dub.gutenberg.config.ElasticsearchConfig;

@SpringBootApplication
@EnableDiscoveryClient
public class Application {
	
	@Value("${elasticsearch-host}")
	private String elasticsearchHost; 

	@Autowired
	private ObjectMapper objectMapper;

	DateFormat dateFormat = DateFormat.getDateInstance();//.getDateTimeInstance();
	
	@PostConstruct
	public void setUp() {
		objectMapper.registerModule(new JavaTimeModule());
		    
	}
	
	@Autowired
	private ElasticsearchConfig elasticsearchConfig;
		
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public RestOperations restTemplate() {
		return new RestTemplate();
	}
	
		
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
