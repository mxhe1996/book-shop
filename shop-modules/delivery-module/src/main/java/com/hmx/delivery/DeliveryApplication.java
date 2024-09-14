package com.hmx.delivery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = {"com.hmx.**.mapper"})
public class DeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryApplication.class, args);
    }
}
