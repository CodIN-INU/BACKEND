package inu.codin.codin.domain.chat.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.domain.chatroom.ChatRoom;
import inu.codin.codin.domain.chat.domain.chatroom.ParticipantInfo;
import inu.codin.codin.domain.chat.dto.chatroom.request.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.exception.ChatRoomErrorCode;
import inu.codin.codin.domain.chat.exception.ChatRoomException;
import inu.codin.codin.domain.chat.exception.ChatRoomExistedException;
import inu.codin.codin.domain.chat.repository.ChatRoomRepository;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomValidator {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public void validate(ChatRoomCreateRequestDto chatRoomCreateRequestDto, ObjectId senderId) {
        validateReceiverExisted(chatRoomCreateRequestDto); //Receiver가 존재하는 지 학인
        validateChatRoomDuplicated(chatRoomCreateRequestDto, senderId); //동일한 채팅방이 있는지 확인
        validateNotSelfChat(chatRoomCreateRequestDto, senderId); //자기 자신과 채팅방을 만들었는지 확인
    }

    private void validateNotSelfChat(ChatRoomCreateRequestDto chatRoomCreateRequestDto, ObjectId senderId) {
        if (senderId.equals(chatRoomCreateRequestDto.getReceiverId()))
            throw new ChatRoomException(ChatRoomErrorCode.CHATROOM_CREATE_MYSELF);
    }

    private void validateChatRoomDuplicated(ChatRoomCreateRequestDto chatRoomCreateRequestDto, ObjectId senderId) {
        Optional<ChatRoom> existedChatroom = chatRoomRepository.findByReferenceIdAndParticipantsContaining(
                chatRoomCreateRequestDto.getReferenceId(), senderId, chatRoomCreateRequestDto.getReceiverId());
        if (existedChatroom.isPresent()) {
            ParticipantInfo participantInfo= existedChatroom.get().getParticipants().getInfo().get(senderId);
            if (participantInfo.isLeaved()) {
                participantInfo.remain();
                chatRoomRepository.save(existedChatroom.get());
            }
            //client 측에서 303 에러를 받아서 해당 채팅방으로 redirect
            throw new ChatRoomExistedException(existedChatroom.get().get_id());
        }
    }

    private void validateReceiverExisted(ChatRoomCreateRequestDto chatRoomCreateRequestDto) {
        userRepository.findById(chatRoomCreateRequestDto.getReceiverId())
                .orElseThrow(() -> {
                    log.error("[Receive 유저 확인 실패] 수신자 ID: {}를 찾을 수 없습니다.", chatRoomCreateRequestDto.getReceiverId());
                    return new NotFoundException("Receive 유저를 찾을 수 없습니다.");
                });
    }

    public void validateParticipantsAllLeaved(String chatRoomId, ChatRoom chatRoom) {
        boolean isAllLeaved = chatRoom.getParticipants().getInfo().values()
                .stream()
                .allMatch(ParticipantInfo::isLeaved);  // 모든 참가자가 떠났는지 확인
        if (isAllLeaved) {
            chatRoom.delete();
            chatRoomRepository.save(chatRoom);
            log.info("[채팅방 삭제] 채팅방 ID: {}에 더 이상 참여자가 없어 채팅방을 삭제합니다.", chatRoomId);
        }
    }
}
