package inu.codin.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CodinAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinAuthServiceApplication.class, args);
    }
}
