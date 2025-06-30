package inu.codin.codin.domain.chat.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.domain.chatroom.event.ChatRoomNotificationEvent;
import inu.codin.codin.domain.chat.dto.chatroom.request.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.dto.chatroom.response.ChatRoomCreateResponseDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @InjectMocks
    ChatRoomService chatRoomService;
    @Mock
    ChatRoomRepository chatRoomRepository;
    @Mock
    SecurityUtils securityUtils;
    @Mock
    ChatRoomValidator chatRoomValidator;
    @Mock
    BlockService blockService;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @BeforeEach
    public void setCustomDetails(){
        CustomUserDetails userDetails = CustomUserDetails.builder().email("test@test.com").build();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("채팅방을 생성합니다.")
    public void 채팅방_생성(){
        //given
        ObjectId senderId = new ObjectId();
        given(SecurityUtils.getCurrentUserId()).willReturn(senderId);

        ChatRoomCreateRequestDto requestDto = ChatRoomCreateRequestDto.builder()
                .roomName("roomName")
                .receiverId("receiverId")
                .referenceId("referenceId")
                .build();
        ChatRoom chatRoom = ChatRoom.of(requestDto, senderId);
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

        //when
        ChatRoomCreateResponseDto createResponseDto = chatRoomService.createChatRoom(requestDto);

        //then
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(eventPublisher, times(1)).publishEvent(any(ChatRoomNotificationEvent.class));
        assertThat(createResponseDto.getChatRoomId()).isEqualTo(chatRoom.get_id().toString());
    }

    @Test
    @DisplayName("채팅방 생성 중 오류 발생 - Receiver가 존재하는지 확인 NotFoundException")
    public void 채팅방_오류_NotFoundException(){

    }

}