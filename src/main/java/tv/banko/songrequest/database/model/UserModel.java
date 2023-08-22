package tv.banko.songrequest.database.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserModel {

    @BsonProperty(value = "broadcaster_id")
    @BsonId
    private final String broadcasterId;
    private final List<ObjectId> rewards;
    private final List<ObjectId> authentications;

    public UserModel(@NotNull String broadcasterId, @NotNull List<ObjectId> rewards,
                     @NotNull List<ObjectId> authentications) {
        this.broadcasterId = broadcasterId;
        this.rewards = rewards;
        this.authentications = authentications;
    }

    public String getBroadcasterId() {
        return broadcasterId;
    }

    public List<ObjectId> getRewards() {
        return rewards;
    }

    public List<ObjectId> getAuthentications() {
        return authentications;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "broadcasterId='" + broadcasterId + '\'' +
                ", rewards=" + rewards +
                ", authentications=" + authentications +
                '}';
    }
}
