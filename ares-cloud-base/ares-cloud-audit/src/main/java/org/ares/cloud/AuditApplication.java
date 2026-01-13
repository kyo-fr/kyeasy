package org.ares.cloud;

import org.ares.cloud.annotation.EnableAresServer;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableAresServer
@EnableDiscoveryClient
public class AuditApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditApplication.class, args);
        System.out.println("审计服务启动成功");
    }
}

