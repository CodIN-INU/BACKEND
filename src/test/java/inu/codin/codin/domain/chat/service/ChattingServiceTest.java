package inu.codin.codin.domain.chat.service;

import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.domain.chatroom.Participants;
import inu.codin.codin.domain.chat.domain.chatting.Chatting;
import inu.codin.codin.domain.chat.domain.chatting.ContentType;
import inu.codin.codin.domain.chat.domain.chatting.event.ChattingArrivedEvent;
import inu.codin.codin.domain.chat.domain.chatting.event.ChattingNotificationEvent;
import inu.codin.codin.domain.chat.dto.chatting.request.ChattingRequestDto;
import inu.codin.codin.domain.chat.dto.chatting.response.ChattingAndUserIdResponseDto;
import inu.codin.codin.domain.chat.repository.ChattingRepository;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChattingServiceTest {

    @InjectMocks
    ChattingService chattingService;

    @Mock
    ChatRoomService chatRoomService;

    @Mock
    ChattingRepository chattingRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    ChatRoom testChatRoom;
    ObjectId chattingId = new ObjectId("64a9f9b8e1c4c3a1d4e5f676");
    ObjectId chatRoomId = new ObjectId("64a9f9b8e1c4c3a1d4e5f677");
    ObjectId senderId = new ObjectId("64a9f9b8e1c4c3a1d4e5f678");
    ObjectId receiverId = new ObjectId("64a9f9b8e1c4c3a1d4e5f679");

    @BeforeEach
    public void createChatRoom(){
        Participants participants = new Participants();
        participants.create(senderId);
        participants.create(receiverId);
        testChatRoom = ChatRoom.builder().participants(participants).build();
        ReflectionTestUtils.setField(testChatRoom, "_id", chatRoomId);
        given(chatRoomService.getChatRoom(chatRoomId.toString())).willReturn(testChatRoom);
    }

    CustomUserDetails userDetails;

    @BeforeEach
    public void setCustomDetails(){
        userDetails = CustomUserDetails.builder().id(senderId).email("test@test.com").build();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("메세지를 전송합니다 - 성공")
    void 메세지_전송_성공(){
        //given
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getAuthorities());
        ChattingRequestDto requestDto = ChattingRequestDto.builder().content("content").contentType(ContentType.TEXT).build();
        given(chattingRepository.save(any(Chatting.class))).willAnswer(invocation -> {
            Chatting savedChatting = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedChatting, "_id", chattingId);
            return savedChatting;
        });

        //when
        chattingService.sendMessage(chatRoomId.toString(), requestDto, authentication);

        //then
        verify(chatRoomService, times(1)).reactivateChatRoomForUser(testChatRoom, senderId);
        verify(chattingRepository, times(1)).save(any(Chatting.class));
        verify(eventPublisher, times(1)).publishEvent(any(ChattingArrivedEvent.class));
        verify(eventPublisher, times(1)).publishEvent(any(ChattingNotificationEvent.class));
    }

    @Test
    @DisplayName("나간적 없는 채팅방의 채팅 내역 반환")
    void 나간적_없는_채팅_내역_반환(){
        //given
        int page = 1;
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        Chatting chatting1 = getChatting();
        Chatting chatting2 = getChatting();
        List<Chatting> chattings = List.of(chatting1, chatting2);
        given(chattingRepository.findAllByChatRoomId(chatRoomId, pageable)).willReturn(chattings);

        //when
        ChattingAndUserIdResponseDto responseDtos = chattingService.getAllMessage(chatRoomId.toString(), page);

        //then
        assertThat(responseDtos.getChatting().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("나간적 있는 채팅방의 채팅내역 반환")
    void 나간적_있는_채팅_내역_반환(){
        //given
        int page = 1;
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        testChatRoom.getParticipants().leave(senderId);
        given(chattingRepository.findAllByChatRoomIdAndCreatedAtAfter(chatRoomId, testChatRoom.getParticipants().getWhenLeaved(senderId), pageable)).willReturn(List.of());

        //when
        ChattingAndUserIdResponseDto responseDtos = chattingService.getAllMessage(chatRoomId.toString(), page);

        //then
        assertThat(responseDtos.getChatting().size()).isEqualTo(0);
    }

    private Chatting getChatting() {
        Chatting chatting1 = Chatting.builder().chatRoomId(chatRoomId).senderId(senderId).build();
        ReflectionTestUtils.setField(chatting1, "_id", chattingId);
        return chatting1;
    }
}