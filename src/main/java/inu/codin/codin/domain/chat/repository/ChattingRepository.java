package inu.codin.codin.domain.chat.repository;

import inu.codin.codin.domain.chat.domain.chatting.Chatting;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChattingRepository extends MongoRepository<Chatting, String> {

    List<Chatting> findAllByChatRoomIdOrderByCreatedAtDesc(ObjectId chatRoomId, Pageable pageable);

    List<Chatting> findAllByChatRoomId(ObjectId id, Pageable pageable);

    List<Chatting> findAllByChatRoomIdAndCreatedAtAfter(ObjectId id, LocalDateTime whenLeaved, Pageable pageable);

}
