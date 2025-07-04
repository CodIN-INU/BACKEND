package inu.codin.codin.domain.chat.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.domain.chatroom.Participants;
import inu.codin.codin.domain.chat.domain.chatroom.event.ChatRoomNotificationEvent;
import inu.codin.codin.domain.chat.dto.chatroom.request.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.dto.chatroom.response.ChatRoomCreateResponseDto;
import inu.codin.codin.domain.chat.exception.ChatRoomErrorCode;
import inu.codin.codin.domain.chat.exception.ChatRoomException;
import inu.codin.codin.domain.chat.exception.ChatRoomExistedException;
import inu.codin.codin.domain.chat.repository.ChatRoomRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @InjectMocks
    ChatRoomService chatRoomService;
    @Mock
    ChatRoomRepository chatRoomRepository;
    @Mock
    ChatRoomValidator chatRoomValidator;
    @Mock
    BlockService blockService;
    @Mock
    ApplicationEventPublisher eventPublisher;

    ObjectId chatRoomId = new ObjectId("64a9f9b8e1c4c3a1d4e5f677");
    ObjectId senderId = new ObjectId("64a9f9b8e1c4c3a1d4e5f678");
    ObjectId receiverId = new ObjectId("64a9f9b8e1c4c3a1d4e5f679");

    @BeforeEach
    public void setCustomDetails(){
        CustomUserDetails userDetails = CustomUserDetails.builder().id(senderId).email("test@test.com").build();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("채팅방을 생성합니다.")
    public void 채팅방_생성(){
        //given
        ChatRoomCreateRequestDto requestDto = ChatRoomCreateRequestDto.builder()
                .receiverId(receiverId.toString())
                .referenceId(new ObjectId().toString())
                .build();

        given(chatRoomRepository.save(any(ChatRoom.class))).willAnswer(invocation -> { //생성된 ChatRoom의 _id를 동적으로 채워줌
            ChatRoom savedChatRoom = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedChatRoom, "_id", chatRoomId);
            return savedChatRoom;
        });

        //when
        ChatRoomCreateResponseDto createResponseDto = chatRoomService.createChatRoom(requestDto);

        //then
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(eventPublisher, times(1)).publishEvent(any(ChatRoomNotificationEvent.class));

        assertThat(createResponseDto.getChatRoomId()).isEqualTo(chatRoomId.toString());
    }

    @Test
    @DisplayName("채팅방 생성 중 오류 발생 - Receiver가 존재하는지 확인 NotFoundException")
    public void 채팅방_오류_NotFoundException(){
        //given
        ChatRoomCreateRequestDto requestDto = mock(ChatRoomCreateRequestDto.class);
        doThrow(new NotFoundException("Receive 유저를 찾을 수 없습니다."))
                .when(chatRoomValidator).validate(requestDto, senderId);

        //when & then
        assertThatThrownBy(() -> chatRoomService.createChatRoom(requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Receive 유저를 찾을 수 없습니다.");

        verify(chatRoomRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("채팅방 생성 중 오류 발생 - 중복된 채팅방이 존재하는 지 확인 ChatRoomExistedException")
    public void 채팅방_오류_ChatRoomExistedException(){
        //given
        ChatRoomCreateRequestDto requestDto = mock(ChatRoomCreateRequestDto.class);
        doThrow(new ChatRoomExistedException(chatRoomId))
                .when(chatRoomValidator).validate(requestDto, senderId);

        //when & then
        assertThatThrownBy(() -> chatRoomService.createChatRoom(requestDto))
                .isInstanceOf(ChatRoomExistedException.class)
                .hasMessage(ChatRoomErrorCode.CHATROOM_EXISTED.message());

        verify(chatRoomRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("채팅방 생성 중 오류 발생 - 자기 자신과 채팅 불가 ChatRoomException")
    void 채팅방_오류_ChatRoomException(){
        //given
        ChatRoomCreateRequestDto requestDto = mock(ChatRoomCreateRequestDto.class);
        doThrow(new ChatRoomException(ChatRoomErrorCode.CHATROOM_CREATE_MYSELF))
                .when(chatRoomValidator).validate(requestDto, senderId);

        //when & then
        assertThatThrownBy(() -> chatRoomService.createChatRoom(requestDto))
                .isInstanceOf(ChatRoomException.class)
                .hasMessage(ChatRoomErrorCode.CHATROOM_CREATE_MYSELF.message());

        verify(chatRoomRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("채팅방 목록 전체 조회")
    void 채팅방_목록_전체_조회(){
        //blockedUser 해결 후 테스트 진행
    }

    @Test
    @DisplayName("채팅방 조회 실패 시 에러 반환")
    void 채팅방_조회_실패_ChatRoomException(){
        //given
        given(chatRoomRepository.findBy_idAndDeletedAtIsNull(chatRoomId)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> chatRoomService.getChatRoom(chatRoomId.toString()))
                .isInstanceOf(ChatRoomException.class)
                .hasMessage(ChatRoomErrorCode.CHATROOM_NOT_FOUND.message());
    }

    @Test
    @DisplayName("채팅방 나가기")
    void 채팅방_나가기(){
        //given
        ChatRoom chatRoom = getChatRoom();
        given(chatRoomRepository.findBy_idAndDeletedAtIsNull(chatRoomId)).willReturn(Optional.ofNullable(chatRoom));

        //when
        chatRoomService.leaveChatRoom(chatRoomId.toString());

        //given
        assertTrue(chatRoom.getParticipants().getInfo().get(senderId).isLeaved());
        verify(chatRoomRepository, times(1)).save(any());
        verify(chatRoomValidator, times(1)).validateParticipantsAllLeaved(any(), any());
    }

    @Test
    @DisplayName("채팅방 알림 설정 true -> false")
    void 채팅방_알림_끄기(){
        //given
        ChatRoom chatRoom = getChatRoom(); //채팅방 생성 시 알림 true
        given(chatRoomRepository.findBy_idAndDeletedAtIsNull(chatRoomId)).willReturn(Optional.of(chatRoom));

        //when
        //알림 false로 설정
        chatRoomService.setNotificationChatRoom(chatRoomId.toString());

        //then
        assertFalse(chatRoom.getParticipants().getInfo().get(senderId).isNotificationsEnabled());
        verify(chatRoomRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("채팅방 알림 설정 false -> true")
    void 채팅방_알림_켜기(){
        //given
        ChatRoom chatRoom = getChatRoom();
        chatRoom.getParticipants().toggleNotification(senderId);
        //채팅방 알림 false
        given(chatRoomRepository.findBy_idAndDeletedAtIsNull(chatRoomId)).willReturn(Optional.of(chatRoom));

        //when
        //알림 true로 설정
        chatRoomService.setNotificationChatRoom(chatRoomId.toString());

        //then
        assertTrue(chatRoom.getParticipants().getInfo().get(senderId).isNotificationsEnabled());
        verify(chatRoomRepository, times(1)).save(any());
    }


    private ChatRoom getChatRoom() {
        Participants participant = new Participants();
        participant.create(senderId);
        return ChatRoom.builder().participants(participant).build();
    }

}