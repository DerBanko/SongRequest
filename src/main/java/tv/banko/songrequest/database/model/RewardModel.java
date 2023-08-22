package tv.banko.songrequest.database.model;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

public class RewardModel {

    @BsonProperty(value = "_id")
    private final ObjectId id;
    private final Type type;
    private Boolean state;
    @BsonProperty(value = "reward_id")
    private String rewardId;

    public RewardModel(@NotNull Type type, @NotNull Boolean state, @NotNull String rewardId) {
        this(new ObjectId(), type, state, rewardId);
    }

    public RewardModel(@NotNull ObjectId id, @NotNull Type type, @NotNull Boolean state, @NotNull String rewardId) {
        this.id = id;
        this.type = type;
        this.state = state;
        this.rewardId = rewardId;
    }

    public ObjectId getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(@NotNull Boolean state) {
        this.state = state;
    }

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(@NotNull String rewardId) {
        this.rewardId = rewardId;
    }

    @Override
    public String toString() {
        return "RedemptionModel{" +
                "id=" + id +
                ", type=" + type +
                ", state=" + state +
                ", rewardId='" + rewardId + '\'' +
                '}';
    }

    public enum Type {
        /**
         * Add a song to the queue
         */
        ADD_SONG,
        /**
         * Skip the song that is currently running
         */
        SKIP_SONG,
        /**
         * Restart the song that is currently running
         */
        RESTART_SONG,
        /**
         * Skip back 15 seconds
         */
        SKIP_BACK,
        /**
         * Skip forward 15 seconds
         */
        SKIP_FORWARD
    }

}
