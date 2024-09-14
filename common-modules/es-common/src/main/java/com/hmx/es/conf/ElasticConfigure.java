package com.hmx.es.conf;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import org.springframework.boot.actuate.autoconfigure.metrics.export.elastic.ElasticProperties;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.hmx.es.conf.properties.ElasticProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ElasticConfigure {

    private final ElasticProperties elasticProperties;

    public ElasticConfigure(ElasticProperties elasticProperties) {
        this.elasticProperties = elasticProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "elasticsearch",name = {"host","port"},havingValue = "true")
    public ElasticsearchClient elasticsearchClient(){
        return new ElasticsearchClient(restClientTransport());

    }

    @Bean
    @ConditionalOnMissingBean(ElasticsearchClient.class)
    public ElasticsearchClient defaultElasticsearchClient(){
        return new ElasticsearchClient(new RestClientTransport(RestClient.builder(new HttpHost("127.0.0.1",9200)).build(),new JacksonJsonpMapper()));
    }

    public RestClientTransport restClientTransport(){
        Map propertyMap = elasticProperties.defaultMap();
        RestClient restClient = RestClient.builder(new HttpHost(propertyMap.get("host").toString(), (int) propertyMap.get("port"))).build();
        return new RestClientTransport(restClient,new JacksonJsonpMapper());
    }


}
