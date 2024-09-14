package com.hmx.es.conf.properties;

import com.hmx.shop.factory.DynamicPropertiesFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class ElasticProperties implements DynamicPropertiesFactory {

    @Value("${elasticsearch.host:127.0.0.1}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private Integer port;


    @Override
    public Map generateMap(String flag) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("host",host);
        properties.put("port",port);
        return properties;
    }
}
