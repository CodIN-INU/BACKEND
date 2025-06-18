package inu.codin.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CodinUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinUserServiceApplication.class, args);
    }
}
