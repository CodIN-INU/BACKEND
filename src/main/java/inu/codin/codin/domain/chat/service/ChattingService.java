package inu.codin.codin.domain.chat.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.domain.chatting.Chatting;
import inu.codin.codin.domain.chat.domain.chatting.event.ChattingArrivedEvent;
import inu.codin.codin.domain.chat.domain.chatting.event.ChattingNotificationEvent;
import inu.codin.codin.domain.chat.dto.chatting.request.ChattingRequestDto;
import inu.codin.codin.domain.chat.dto.chatting.response.ChattingAndUserIdResponseDto;
import inu.codin.codin.domain.chat.dto.chatting.response.ChattingResponseDto;
import inu.codin.codin.domain.chat.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.repository.ChattingRepository;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import inu.codin.codin.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChattingService {

    private final ChatRoomService chatRoomService;
    private final ChattingRepository chattingRepository;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChattingResponseDto sendMessage(String chatRoomId, ChattingRequestDto chattingRequestDto, Authentication authentication) {
        ChatRoom chatRoom = chatRoomService.getChatRoom(chatRoomId);
        ObjectId userId = ((CustomUserDetails) authentication.getPrincipal()).getId();

        //상대가 채팅방을 나간 상태라면 다시 활성화하여 채팅 시작
        chatRoomService.reactivateChatRoomForUser(chatRoom, userId);

        int unreadCount = getUnreadCount(chatRoom);
        Chatting chatting = Chatting.of(chatRoom.get_id(), chattingRequestDto, userId, unreadCount);
        chattingRepository.save(chatting);

        log.info("[메시지 전송 성공] 메시지: [{}], 송신자 ID: {}, 채팅방 ID: {}", chattingRequestDto.getContent(), userId, chatRoomId);

        publishUnreadCountAndNotify(chatting, chatRoom, userId);
        return ChattingResponseDto.of(chatting);
    }

    private void publishUnreadCountAndNotify(Chatting chatting, ChatRoom chatRoom, ObjectId userId) {
        //미접속 상태의 유저들에게 unread 개수 업데이트 및 마지막 대화 내용 업데이트
        eventPublisher.publishEvent(new ChattingArrivedEvent(this, chatting, chatRoom));
        //알림 보내기
        eventPublisher.publishEvent(new ChattingNotificationEvent(this, userId, chatRoom));
    }

    public ChattingAndUserIdResponseDto getAllMessage(String chatRoomId, int page) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        ChatRoom chatRoom = chatRoomService.getChatRoom(chatRoomId);

        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        List<ChattingResponseDto> chattingResponseDto = getChattingSinceRejoin(chatRoom.get_id(), chatRoom, userId, pageable);
        log.info("[메시지 조회 성공] 채팅방 ID: {}, 메시지 개수: {}", chatRoomId, chattingResponseDto.size());

        return new ChattingAndUserIdResponseDto(chattingResponseDto, userId.toString());
    }

    private List<ChattingResponseDto> getChattingSinceRejoin(ObjectId chatRoomId, ChatRoom chatRoom, ObjectId userId, Pageable pageable) {
        LocalDateTime whenLeaved = chatRoom.getParticipants().getWhenLeaved(userId);
        return getChattingStream(chatRoomId, whenLeaved, pageable)
                .map(ChattingResponseDto::of)
                .toList();
    }

    private Stream<Chatting> getChattingStream(ObjectId chatRoomId, LocalDateTime whenLeaved, Pageable pageable) {
        if (whenLeaved != null)
            return chattingRepository.findAllByChatRoomIdAndCreatedAtAfter(chatRoomId, whenLeaved, pageable).stream();
        else
            return chattingRepository.findAllByChatRoomId(chatRoomId, pageable).stream();
    }

    public List<String> sendImageMessage(List<MultipartFile> chatImages) {
        log.info("[이미지 메시지 전송] 이미지 개수: {}", chatImages.size());
        List<String> imageUrls = s3Service.handleImageUpload(chatImages);
        log.info("[이미지 메시지 전송 성공] 업로드된 이미지 URL 개수: {}", imageUrls.size());
        return imageUrls;
    }

    private int getUnreadCount(ChatRoom chatRoom) {
        //채팅방에 접속해 있는 사람 수 빼기 (읽은 count)
        int countOfParticipating = chatRoom.getParticipants().getCountOfConnecting();
        return chatRoom.getParticipants().size() - countOfParticipating;
    }
}
