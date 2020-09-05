package com.dub.gutenberg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ElasticsearchConfig {

	//The config parameters for the connection
	@Value("${elasticsearch-host}")
	private String HOST;
	
	@Value("${elasticsearch-port-one}")
	private int PORT_ONE;
	
	@Value("${elasticsearch-port-two}")
	private int PORT_TWO;
	
	@Value("${elasticsearch-scheme}")
	private String SCHEME;
	
	
    public String getHost() {
		return HOST;
	}


	public int getPortOne() {
		return PORT_ONE;
	}


	public int getPortTwo() {
		return PORT_TWO;
	}


	public String getScheme() {
		return SCHEME;
	}
}
