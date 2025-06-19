package inu.codin.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class CodinServiceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodinServiceRegistryApplication.class, args);
    }
}
