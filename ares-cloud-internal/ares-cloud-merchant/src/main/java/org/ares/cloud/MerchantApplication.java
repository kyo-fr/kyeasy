package org.ares.cloud;

import org.ares.cloud.annotation.EnableAresServer;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableAresServer
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"org.ares.cloud.api.base","org.ares.cloud.api.user","org.ares.cloud.api.order","org.ares.cloud.api.product","org.ares.cloud.api.merchant","org.ares.cloud.api.msg_center","org.ares.cloud.api.gateway"})
public class MerchantApplication {

    public static void main(String[] args) {
        SpringApplication.run(MerchantApplication.class, args);
        System.out.println("商户服务启动成功");
    }

}
