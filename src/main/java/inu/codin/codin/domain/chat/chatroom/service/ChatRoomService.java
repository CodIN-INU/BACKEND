package inu.codin.codin.domain.chat.chatroom.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomCreateRequestDto;
import inu.codin.codin.domain.chat.chatroom.dto.ChatRoomListResponseDto;
import inu.codin.codin.domain.chat.chatroom.entity.ChatRoom;
import inu.codin.codin.domain.chat.chatroom.entity.Participants;
import inu.codin.codin.domain.chat.chatroom.exception.ChatRoomNotFoundException;
import inu.codin.codin.domain.chat.chatroom.repository.ChatRoomRepository;
import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import inu.codin.codin.domain.chat.chatting.repository.CustomChattingRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.domain.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final CustomChattingRepository customChattingRepository;
    private final UserRepository userRepository;

    public Map<String, String> createChatRoom(ChatRoomCreateRequestDto chatRoomCreateRequestDto, UserDetails userDetails) {
        ObjectId senderId = ((CustomUserDetails) userDetails).getId();
        userRepository.findById(new ObjectId(chatRoomCreateRequestDto.getReceiverId()))
                .orElseThrow(() -> new NotFoundException("Receive 유저를 찾을 수 없습니다.")); //Receive 유저에 대한 유효성 검사
        ChatRoom chatRoom = ChatRoom.of(chatRoomCreateRequestDto, senderId);
        chatRoomRepository.save(chatRoom);
        Map<String, String> response = new HashMap<>();
        response.put("chatRoomId", chatRoom.get_id().toString());
        return response;
    }

    public List<ChatRoomListResponseDto> getAllChatRoomByUser(UserDetails userDetails) {
        ObjectId userId = ((CustomUserDetails) userDetails).getId();
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipant(userId);
        return chatRooms.stream()
                .map(chatRoom -> {
                    Chatting chat = customChattingRepository.findMostRecentByChatRoomId(chatRoom.get_id());
                    return ChatRoomListResponseDto.of(chatRoom, chat);
                }).toList();
    }

    public void leaveChatRoom(String chatRoomId, UserDetails userDetails) {
        ObjectId userId = ((CustomUserDetails) userDetails).getId();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));
        boolean isRemoved = chatRoom.getParticipants()
                .removeIf(participant -> participant.getUserId().equals(userId));
        if (!isRemoved) throw new ChatRoomNotFoundException("회원이 포함된 채팅방을 찾을 수 없습니다.");

        if (chatRoom.getParticipants().isEmpty()) {
            chatRoom.delete();
            log.info("[LeaveChatRoom] 채팅방에 참여자가 없어 채팅방 삭제");
        }
        chatRoomRepository.save(chatRoom);
    }

    public void setNotificationChatRoom(String chatRoomId, UserDetails userDetails) {
        ObjectId userId = ((CustomUserDetails) userDetails).getId();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));
        chatRoom.getParticipants().stream()
                .filter(participants -> participants.getUserId().equals(userId))
                .forEach(Participants::updateNotification);
        chatRoomRepository.save(chatRoom);
    }
}
