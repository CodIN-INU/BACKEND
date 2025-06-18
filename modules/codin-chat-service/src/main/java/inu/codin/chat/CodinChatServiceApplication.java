package inu.codin.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CodinChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinChatServiceApplication.class, args);
    }
}
