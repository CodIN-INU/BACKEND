package inu.codin.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CodinContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinContentServiceApplication.class, args);
    }
}
