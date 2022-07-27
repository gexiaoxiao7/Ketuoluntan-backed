package com.suibe.suibe_mma;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.suibe.suibe_mma.mapper")
public class SuibeMmaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuibeMmaApplication.class, args);
    }

}
