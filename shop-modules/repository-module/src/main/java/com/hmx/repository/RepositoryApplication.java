package com.hmx.repository;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@MapperScan("com.hmx.**.mapper")
public class RepositoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepositoryApplication.class,args);
    }

}
