package org.ares.cloud;

import org.ares.cloud.annotation.EnableAresServer;
import org.ares.cloud.properties.FirebaseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableAresServer
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"org.ares.cloud.api.user","org.ares.cloud.api.merchant"})
@EnableConfigurationProperties(FirebaseProperties.class)
public class AuthCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthCenterApplication.class, args);
        System.out.println("认证服务启动成功");
    }
}