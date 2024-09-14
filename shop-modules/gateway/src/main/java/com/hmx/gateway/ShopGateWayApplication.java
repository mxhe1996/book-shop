package com.hmx.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ShopGateWayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopGateWayApplication.class,args);
    }
}
