package inu.codin.codin.domain.chat.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.domain.chatroom.Participants;
import inu.codin.codin.domain.chat.dto.chatroom.request.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.exception.ChatRoomErrorCode;
import inu.codin.codin.domain.chat.exception.ChatRoomException;
import inu.codin.codin.domain.chat.exception.ChatRoomExistedException;
import inu.codin.codin.domain.chat.repository.ChatRoomRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomValidatorTest {

    @InjectMocks
    ChatRoomValidator chatRoomValidator;

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    UserRepository userRepository;

    ObjectId senderId = new ObjectId();
    ChatRoomCreateRequestDto requestDto = ChatRoomCreateRequestDto.builder()
            .referenceId(new ObjectId().toString())
            .receiverId(senderId.toString()).build();

    @Test
    @DisplayName("송신자와 수신자가 같을 경우 에러 반환")
    void 송신자_수신자_동일(){
        //given
        given(userRepository.findById(senderId)).willReturn(Optional.ofNullable(mock(UserEntity.class)));
        given(chatRoomRepository.findByReferenceIdAndParticipantsContaining(any(), any(), any())).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> chatRoomValidator.validate(requestDto, senderId))
                .isInstanceOf(ChatRoomException.class)
                .hasMessage(ChatRoomErrorCode.CHATROOM_CREATE_MYSELF.message());
    }

    @Test
    @DisplayName("채팅방이 이미 존재하는 경우 에러 반환")
    void 채팅방_이미_존재(){
        //given
        //참여자 생성
        ChatRoom chatroom = getChatRoom();

        given(userRepository.findById(senderId)).willReturn(Optional.ofNullable(mock(UserEntity.class)));
        given(chatRoomRepository.findByReferenceIdAndParticipantsContaining(any(), any(), any())).willReturn(Optional.of(chatroom));

        //when & then
        assertThatThrownBy(() -> chatRoomValidator.validate(requestDto, senderId))
                .isInstanceOf(ChatRoomExistedException.class)
                .hasMessage(ChatRoomErrorCode.CHATROOM_EXISTED.message());

        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방이 이미 존재하는 경우, 상대방이 떠난 상태라면 다시 불러온 후 에러 반환")
    void 채팅방_이미_존재_상대방_불러오기(){
        //given
        //참여자 생성
        ChatRoom chatroom = getChatRoomWithLeaveParticipants();

        given(userRepository.findById(senderId)).willReturn(Optional.ofNullable(mock(UserEntity.class)));
        given(chatRoomRepository.findByReferenceIdAndParticipantsContaining(any(), any(), any())).willReturn(Optional.of(chatroom));

        //when & then
        assertThatThrownBy(() -> chatRoomValidator.validate(requestDto, senderId))
                .isInstanceOf(ChatRoomExistedException.class)
                .hasMessage(ChatRoomErrorCode.CHATROOM_EXISTED.message());

        //채팅방에 다시 불러왔기 때문에 leave 상태가 false가 되었음
        assertFalse(chatroom.getParticipants().getInfo().get(senderId).isLeaved());
        verify(chatRoomRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Receiver가 존재하지 않을 경우 에러 반환")
    void RECEIVER_존재하지_않음(){
        //given
        String errorMessage = "Receive 유저를 찾을 수 없습니다.";
        doThrow(new NotFoundException(errorMessage))
                .when(userRepository).findById(senderId);

        //when & then
        assertThatThrownBy(() -> chatRoomValidator.validate(requestDto, senderId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("채팅방의 참여자가 모두 떠났을 경우 채팅방 삭제")
    void 채팅방_참여자가_없을경우_삭제(){
        //given
        ChatRoom chatRoom = getChatRoomWithLeaveParticipants();

        //when
        chatRoomValidator.validateParticipantsAllLeaved(new ObjectId().toString(), chatRoom);

        //then
        assertNotNull(chatRoom.getDeletedAt());
        verify(chatRoomRepository, times(1)).save(any());
    }

    private ChatRoom getChatRoom() {
        Participants participants = new Participants();
        participants.create(senderId);
        return ChatRoom.builder().participants(participants).build();
    }

    private ChatRoom getChatRoomWithLeaveParticipants() {
        Participants participants = new Participants();
        participants.create(senderId);
        participants.getInfo().get(senderId).leave();
        return ChatRoom.builder().participants(participants).build();
    }
}