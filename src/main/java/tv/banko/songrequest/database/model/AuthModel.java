package tv.banko.songrequest.database.model;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

public class AuthModel {

    @BsonProperty(value = "_id")
    private final ObjectId id;
    private final Type type;
    private String accessToken;
    private String refreshToken;

    public AuthModel(@NotNull Type type, @NotNull String accessToken, @NotNull String refreshToken) {
        this(new ObjectId(), type, accessToken, refreshToken);
    }

    public AuthModel(@NotNull ObjectId id, @NotNull Type type, @NotNull String accessToken, @NotNull String refreshToken) {
        this.id = id;
        this.type = type;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public ObjectId getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(@NotNull String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(@NotNull String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "AuthModel{" +
                "id=" + id +
                ", type=" + type +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }

    public enum Type {
        TWITCH,
        SPOTIFY
    }

}
