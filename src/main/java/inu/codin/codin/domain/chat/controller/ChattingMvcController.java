package inu.codin.codin.domain.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChattingMvcController {

    //채팅 테스트를 위한 MVC
    @GetMapping("/chat")
    public String chatHtml(){
        return "chat";
    }

    @GetMapping("/chat/image")
    public String chatImageHtml(){
        return "chatImage";
    }

    @GetMapping("/chat/room")
    public String chatroomHtml(){
        return "chatroom";
    }
}
