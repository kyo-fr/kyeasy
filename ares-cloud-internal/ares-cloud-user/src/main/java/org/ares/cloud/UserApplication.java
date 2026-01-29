package org.ares.cloud;

import org.ares.cloud.annotation.EnableAresServer;
import org.ares.cloud.api.auth.properties.HsmProperties;
import org.ares.cloud.user.properties.SuperAdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableAresServer
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"org.ares.cloud.api.base", "org.ares.cloud.api.auth", "org.ares.cloud.api.user", "org.ares.cloud.api.gateway"})
@ComponentScan(basePackages = {"org.ares.cloud.**","org.ares.cloud.**"})
@EnableConfigurationProperties({SuperAdminProperties.class, HsmProperties.class})
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        System.out.println("用户服务启动成功");

    }
}
