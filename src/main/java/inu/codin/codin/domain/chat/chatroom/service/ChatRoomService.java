package inu.codin.codin.domain.chat.chatroom.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomListResponseDto;
import inu.codin.codin.domain.chat.chatroom.dto.event.ChatRoomNotificationEvent;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.entity.ParticipantInfo;
import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomCreateFailException;
import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomNotFoundException;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.repository.CustomChattingRepository;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    private final BlockService blockService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;


    public Map<String, String> createChatRoom(ChatRoomCreateRequestDto chatRoomCreateRequestDto) {
        ObjectId senderId = SecurityUtils.getCurrentUserId();
        if (senderId.toString().equals(chatRoomCreateRequestDto.getReceiverId())){
            throw new ChatRoomCreateFailException("자기 자신과는 채팅방을 생성할 수 없습니다.");
        }
        log.info("[채팅방 생성 요청] 송신자 ID: {}, 수신자 ID: {}", senderId, chatRoomCreateRequestDto.getReceiverId());

        userRepository.findById(new ObjectId(chatRoomCreateRequestDto.getReceiverId()))
                .orElseThrow(() -> {
                    log.error("[Receive 유저 확인 실패] 수신자 ID: {}를 찾을 수 없습니다.", chatRoomCreateRequestDto.getReceiverId());
                    return new NotFoundException("Receive 유저를 찾을 수 없습니다.");
                });
        log.info("[Receive 유저 확인 완료] 수신자 ID: {}", chatRoomCreateRequestDto.getReceiverId());

        ChatRoom chatRoom = ChatRoom.of(chatRoomCreateRequestDto, senderId);
        chatRoomRepository.save(chatRoom);
        log.info("[채팅방 생성 완료] 채팅방 ID: {}, 송신자 ID: {}, 수신자 ID: {}", chatRoom.get_id(), senderId, chatRoomCreateRequestDto.getReceiverId());

        eventPublisher.publishEvent(new ChatRoomNotificationEvent(this,
                chatRoom.get_id(), new ObjectId(chatRoomCreateRequestDto.getReceiverId()), chatRoom.getParticipants()));

        Map<String, String> response = new HashMap<>();
        response.put("chatRoomId", chatRoom.get_id().toString());
        log.info("[채팅방 생성 응답] 생성된 채팅방 ID: {}", chatRoom.get_id());
        return response;
    }

    public List<ChatRoomListResponseDto> getAllChatRoomByUser(UserDetails userDetails) {
        ObjectId userId = ((CustomUserDetails) userDetails).getId();
        log.info("[유저의 채팅방 조회] 유저 ID: {}", userId);
        // 차단 목록 조회
        List<ObjectId> blockedUsersId = blockService.getBlockedUsers();

        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipant(userId);
        log.info("[채팅방 조회 결과] 유저 ID: {}가 참여 중인 채팅방 개수: {}", userId, chatRooms.size());
        return chatRooms.stream()
                .filter(chatRoom -> chatRoom.getParticipants().getInfo().keySet().stream()
                        .noneMatch(blockedUsersId::contains))
                .map(chatRoom -> ChatRoomListResponseDto.of(chatRoom, userId)).toList();
    }

    public void leaveChatRoom(String chatRoomId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        log.info("[채팅방 탈퇴 요청] 유저 ID: {}, 채팅방 ID: {}", userId, chatRoomId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    log.error("[채팅방 확인 실패] 채팅방 ID: {}를 찾을 수 없습니다.", chatRoomId);
                    return new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다.");
                });

        Map<ObjectId, ParticipantInfo> info = chatRoom.getParticipants().getInfo();
        log.info("[채팅방 확인] 채팅방 ID: {}, 참여자 수: {}", chatRoomId, info.size());

        if (info.containsKey(userId)){
            info.remove(userId);
        } else {
            log.warn("[채팅방 탈퇴 실패] 유저 ID: {}는 채팅방에 참여하지 않았습니다.", userId);
            throw new ChatRoomNotFoundException("회원이 포함된 채팅방을 찾을 수 없습니다.");
        }
        log.info("[채팅방 탈퇴 성공] 유저 ID: {}가 채팅방에서 탈퇴", userId);

//        if (chatRoom.getParticipants().isEmpty()) {
//            chatRoom.delete();
//            log.info("[채팅방 삭제] 채팅방 ID: {}에 더 이상 참여자가 없어 채팅방을 삭제합니다.", chatRoomId);
//        }
        chatRoomRepository.save(chatRoom);
        log.info("[채팅방 삭제 후 저장] 채팅방 ID: {} 저장 완료", chatRoomId);

        }

    public void setNotificationChatRoom(String chatRoomId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        log.info("[알림 설정 요청] 유저 ID: {}, 채팅방 ID: {}", userId, chatRoomId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    log.error("[알림 설정 실패] 채팅방을 찾을 수 없습니다. 채팅방 ID: {}", chatRoomId);
                    return new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다.");
                });

        Map<ObjectId, ParticipantInfo> info = chatRoom.getParticipants().getInfo();
        log.info("[채팅방 확인] 채팅방 ID: {}, 참여자 수: {}", chatRoomId, info.size());

        info.get(userId).updateNotification();
        chatRoomRepository.save(chatRoom);
        log.info("[알림 설정 완료] 채팅방 ID: {}에 알림 설정 완료", chatRoomId);
    }
}
