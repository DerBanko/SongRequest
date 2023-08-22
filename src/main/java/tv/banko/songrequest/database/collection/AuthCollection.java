package tv.banko.songrequest.database.collection;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import tv.banko.songrequest.database.Database;
import tv.banko.songrequest.database.model.AuthModel;
import tv.banko.songrequest.database.model.RewardModel;
import tv.banko.songrequest.database.subscriber.EmptySubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AuthCollection {

    private final Database database;

    public AuthCollection(@NotNull Database database) {
        this.database = database;
    }

    public void setAuth(@NotNull AuthModel auth) {
        MongoCollection<AuthModel> collection = this.getCollection();
        collection.replaceOne(Filters.eq("_id", auth.getId()), auth,
                new ReplaceOptions().upsert(true)).subscribe(new EmptySubscriber<>());
    }

    public CompletableFuture<AuthModel> getAuth(@NotNull ObjectId id) {
        CompletableFuture<AuthModel> future = new CompletableFuture<>();
        MongoCollection<AuthModel> collection = this.getCollection();
        collection.find(Filters.eq("_id", id)).first().subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(AuthModel authModel) {
                future.complete(authModel);
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
                future.completeExceptionally(new RuntimeException("No auth found"));
            }
        });
        return future;
    }

    public CompletableFuture<List<AuthModel>> getAuths(@NotNull List<ObjectId> ids) {
        CompletableFuture<List<AuthModel>> future = new CompletableFuture<>();
        MongoCollection<AuthModel> collection = this.getCollection();

        List<AuthModel> list = new ArrayList<>();

        collection.find(Filters.in("_id", ids)).first().subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(AuthModel authModel) {
                list.add(authModel);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                future.complete(list);
            }
        });
        return future;
    }

    private MongoCollection<AuthModel> getCollection() {
        return this.database.getDatabase().getCollection("auth", AuthModel.class);
    }
}
