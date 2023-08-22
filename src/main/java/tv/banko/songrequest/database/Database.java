package tv.banko.songrequest.database;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.SongRequest;
import tv.banko.songrequest.database.collection.AuthCollection;
import tv.banko.songrequest.database.collection.RewardCollection;
import tv.banko.songrequest.database.collection.UserCollection;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private final SongRequest request;
    private final MongoDatabase database;

    private final AuthCollection auth;
    private final RewardCollection reward;
    private final UserCollection user;

    public Database(@NotNull SongRequest request) {
        this.request = request;

        Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);

        try (MongoClient client = MongoClients.create("mongodb://database:27017")) {
            this.database = client.getDatabase("songrequest");
            this.auth = new AuthCollection(this);
            this.reward = new RewardCollection(this);
            this.user = new UserCollection(this);
        }
    }

    public AuthCollection getAuth() {
        return auth;
    }

    public RewardCollection getReward() {
        return reward;
    }

    public UserCollection getUser() {
        return user;
    }

    public SongRequest getRequest() {
        return request;
    }

    public MongoDatabase getDatabase() {
        return database;
    }
}
