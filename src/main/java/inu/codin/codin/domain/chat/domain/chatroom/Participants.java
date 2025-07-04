package inu.codin.codin.domain.chat.domain.chatroom;

import inu.codin.codin.domain.chat.exception.ChatRoomErrorCode;
import inu.codin.codin.domain.chat.exception.ChatRoomException;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Participants {

    private Map<ObjectId, ParticipantInfo> info = new ConcurrentHashMap<>();

    public void create(ObjectId memberId){
        info.put(memberId, ParticipantInfo.enter(memberId));
    }

    /**
     * 메세지를 받는 유저가 끊겨 있는 상태라면 unreadCount 업데이트 후 해당 유저 정보 반환
     * @param senderId 메세지를 보내는 유저 id
     * @return 메세지를 받는 유저들의 정보 리스트
     */
    public List<ReceiverInfo> getDisconnectedUsersAndUpdateUnreadCount(ObjectId senderId) {
        return info.keySet().stream()
                .filter(receiverId -> !receiverId.equals(senderId))
                .map(info::get)
                .filter(receiverInfo -> !receiverInfo.isConnected())
                .map(receiverInfo -> {
                    receiverInfo.incrementUnreadCount();
                    return new ReceiverInfo(receiverInfo.getUserId(), receiverInfo.getUnreadCount());
                })
                .toList();
    }

    private ParticipantInfo findParticipant(ObjectId userId) {
        return Optional.ofNullable(info.get(userId))
                .orElseThrow(() -> new ChatRoomException(ChatRoomErrorCode.PARTICIPANTS_NOT_FOUND));
    }

    public void enter(ObjectId userId) {
        findParticipant(userId).connect();
    }

    public void exit(ObjectId userId) {
        findParticipant(userId).disconnect();
    }

    public void leave(ObjectId userId) {
        findParticipant(userId).leave();
    }

    public void toggleNotification(ObjectId userId) {
        findParticipant(userId).updateNotification();
    }

    public List<ObjectId> getUsersToNotify(ObjectId senderId) {
        return info.values().stream()
                .filter(participantInfo -> participantInfo.isNotified(senderId))
                .map(ParticipantInfo::getUserId).toList();
    }

    public LocalDateTime getWhenLeaved(ObjectId userId) {
        return findParticipant(userId).getWhenLeaved();
    }

    public List<ParticipantInfo> remainReceiver(ObjectId userId) {
        List<ParticipantInfo> participantInfos = findReceiversToRemain(userId);
        participantInfos.forEach(ParticipantInfo::remain);
        return participantInfos;
    }

    private List<ParticipantInfo> findReceiversToRemain(ObjectId userId){
        return info.values().stream()
                .filter(info -> !info.getUserId().equals(userId) && info.isLeaved())
                .toList();
    }

    public int getCountOfConnecting() {
        return (int) info.values().stream()
                .filter(ParticipantInfo::isConnected)
                .count();
    }

    public int size() {
        return info.size();
    }

    public int getUnreadCount(ObjectId userId) {
        return findParticipant(userId).getUnreadCount();
    }

    public boolean checkAllLeaved() {
        return info.values().stream().allMatch(ParticipantInfo::isLeaved);
    }

    public boolean isLeaved(ObjectId senderId) {
        return findParticipant(senderId).isLeaved();
    }

    public void remain(ObjectId senderId) {
        findParticipant(senderId).remain();
    }

    public record ReceiverInfo(ObjectId receiverId, int unreadMessage){}
}
