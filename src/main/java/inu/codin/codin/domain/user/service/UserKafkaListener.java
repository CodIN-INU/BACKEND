package inu.codin.codin.domain.user.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.user.dto.UserReply;
import inu.codin.codin.domain.user.dto.UserRequest;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserKafkaListener {

    private final KafkaTemplate<String, UserReply> kafkaTemplate;
    private final UserRepository userRepository;

    @KafkaListener(
            topics = "ticketing-user-request",
            containerFactory = "userRequestListenerContainerFactory"
    )
    @SendTo("ticketing-user-reply")
    public UserReply handleUserRequest(UserRequest req) {
        try {
            UserEntity user = userRepository.findByEmail(req.username())
                    .orElseThrow(() -> new NotFoundException("User not found"));
            return new UserReply(req.requestId(), user.get_id().toString(), user.getName());
        } catch (Exception ex) {
            log.warn("[handleUserRequest] 요청 처리 실패 (requestId={}): {}", req.requestId(), ex.toString());
            return null;
        }
    }
}
