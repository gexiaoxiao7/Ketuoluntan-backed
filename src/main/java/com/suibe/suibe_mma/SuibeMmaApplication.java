package com.suibe.suibe_mma;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 */
@SpringBootApplication
@MapperScan("com.suibe.suibe_mma.mapper")
public class SuibeMmaApplication {
    /**
     * 项目main方法
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SuibeMmaApplication.class, args);
    }

}
