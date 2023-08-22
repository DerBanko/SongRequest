package tv.banko.songrequest.database.collection;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import tv.banko.songrequest.database.Database;
import tv.banko.songrequest.database.model.UserModel;
import tv.banko.songrequest.database.subscriber.EmptySubscriber;

import java.util.concurrent.CompletableFuture;

public class UserCollection {

    private final Database database;

    public UserCollection(@NotNull Database database) {
        this.database = database;
    }

    public void setUser(@NotNull UserModel user) {
        MongoCollection<UserModel> collection = this.getCollection();
        collection.replaceOne(Filters.eq("broadcaster_id", user.getBroadcasterId()), user,
                new ReplaceOptions().upsert(true)).subscribe(new EmptySubscriber<>());
    }

    public CompletableFuture<UserModel> getUser(@NotNull String broadcasterId) {
        CompletableFuture<UserModel> future = new CompletableFuture<>();
        MongoCollection<UserModel> collection = this.getCollection();
        collection.find(Filters.eq("broadcaster_id", broadcasterId)).first().subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(UserModel userModel) {
                future.complete(userModel);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                if (future.isDone()) {
                    return;
                }
                future.completeExceptionally(new RuntimeException("No broadcaster found"));
            }
        });
        return future;
    }

    private MongoCollection<UserModel> getCollection() {
        return this.database.getDatabase().getCollection("user", UserModel.class);
    }
}
