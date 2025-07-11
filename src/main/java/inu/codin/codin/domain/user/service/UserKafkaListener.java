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
        // 1) DB 또는 서비스 호출로 userId 조회
        UserEntity userEntity = userRepository.findByEmail(req.username())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String userId = userEntity.get_id().toString();
        String name = userEntity.getName();

        log.info("[handleUserRequest] Received request for user {} with id {}", req.username(), userId);

        // 2) 응답 발행
        return new UserReply(req.requestId(), userId, name);
    }
}
