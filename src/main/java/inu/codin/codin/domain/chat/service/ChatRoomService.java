package inu.codin.codin.domain.chat.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.chat.domain.chatroom.ParticipantInfo;
import inu.codin.codin.domain.chat.dto.chatroom.request.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.dto.chatroom.response.ChatRoomCreateResponseDto;
import inu.codin.codin.domain.chat.dto.chatroom.response.ChatRoomListResponseDto;
import inu.codin.codin.domain.chat.domain.chatroom.event.ChatRoomNotificationEvent;
import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.exception.ChatRoomErrorCode;
import inu.codin.codin.domain.chat.exception.ChatRoomException;
import inu.codin.codin.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomValidator chatRoomValidator;
    private final BlockService blockService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatRoomCreateResponseDto createChatRoom(ChatRoomCreateRequestDto chatRoomCreateRequestDto) {
        ObjectId senderId = SecurityUtils.getCurrentUserId();

        chatRoomValidator.validate(chatRoomCreateRequestDto, senderId);

        log.info("[채팅방 생성 요청] 송신자 ID: {}, 수신자 ID: {}", senderId, chatRoomCreateRequestDto.getReceiverId());
        ChatRoom chatRoom = ChatRoom.of(chatRoomCreateRequestDto, senderId);
        chatRoomRepository.save(chatRoom);
        log.info("[채팅방 생성 완료] 채팅방 ID: {}, 송신자 ID: {}, 수신자 ID: {}", chatRoom.get_id(), senderId, chatRoomCreateRequestDto.getReceiverId());

        eventPublisher.publishEvent(new ChatRoomNotificationEvent(
                this, chatRoom.get_id(), chatRoomCreateRequestDto.getReceiverId(), chatRoom.getParticipants()));

        return new ChatRoomCreateResponseDto(chatRoom.get_id().toString());
    }

    public List<ChatRoomListResponseDto> getAllChatRoomByUser() {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        log.info("[유저의 채팅방 조회] 유저 ID: {}", userId);

        // 차단 목록 조회
        List<ObjectId> blockedUsersId = blockService.getBlockedUsers();
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantIsNotLeavedAndDeletedIsNull(userId);
        log.info("[채팅방 조회 결과] 유저 ID: {}가 참여 중인 채팅방 개수: {}", userId, chatRooms.size());
        return chatRooms.stream()
                .filter(chatRoom -> chatRoom.getParticipants().getNoneMatchBlocked(blockedUsersId))
                .sorted(Comparator.comparing(ChatRoom::getCurrentMessageDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(chatRoom -> ChatRoomListResponseDto.of(chatRoom, userId))
                .toList();
    }

    @Transactional
    public void leaveChatRoom(String chatRoomId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        log.info("[채팅방 나가기 요청] 유저 ID: {}, 채팅방 ID: {}", userId, chatRoomId);

        ChatRoom chatRoom = getChatRoom(chatRoomId);
        chatRoom.getParticipants().leave(userId);
        chatRoomRepository.save(chatRoom);
        log.info("[채팅방 나가기 완료] 유저 ID: {}, 채팅방 ID: {}", userId, chatRoomId);

        //채팅방 참여자가 모두 나갔는지 확인
        chatRoomValidator.validateParticipantsAllLeaved(chatRoomId, chatRoom);
    }

    @Transactional
    public void setNotificationChatRoom(String chatRoomId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        log.info("[알림 설정 요청] 유저 ID: {}, 채팅방 ID: {}", userId, chatRoomId);

        ChatRoom chatRoom = getChatRoom(chatRoomId);
        chatRoom.getParticipants().toggleNotification(userId);
        chatRoomRepository.save(chatRoom);
        log.info("[알림 설정 완료] 채팅방 ID: {}에 알림 설정 완료", chatRoomId);
    }

    public ChatRoom getChatRoom(String chatRoomId) {
        return chatRoomRepository.findBy_idAndDeletedAtIsNull(new ObjectId(chatRoomId))
            .orElseThrow(() -> {
                log.error("[채팅방 확인 실패] 채팅방 ID: {}를 찾을 수 없습니다.", chatRoomId);
                return new ChatRoomException(ChatRoomErrorCode.CHATROOM_NOT_FOUND);
            });
    }

    void reactivateChatRoomForUser(ChatRoom chatRoom, ObjectId userId) {
        if (chatRoom.isExistedReactivateParticipants(userId))
            chatRoomRepository.save(chatRoom);
    }
}
