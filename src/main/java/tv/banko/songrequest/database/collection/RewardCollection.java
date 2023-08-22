package tv.banko.songrequest.database.collection;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import tv.banko.songrequest.database.Database;
import tv.banko.songrequest.database.model.RewardModel;
import tv.banko.songrequest.database.subscriber.EmptySubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RewardCollection {

    private final Database database;

    public RewardCollection(@NotNull Database database) {
        this.database = database;
    }

    public void setReward(@NotNull RewardModel reward) {
        MongoCollection<RewardModel> collection = this.getCollection();
        collection.replaceOne(Filters.eq("_id", reward.getId()), reward,
                new ReplaceOptions().upsert(true)).subscribe(new EmptySubscriber<>());
    }

    public CompletableFuture<RewardModel> getReward(@NotNull ObjectId id) {
        CompletableFuture<RewardModel> future = new CompletableFuture<>();
        MongoCollection<RewardModel> collection = this.getCollection();
        collection.find(Filters.eq("_id", id)).first().subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(RewardModel rewardModel) {
                future.complete(rewardModel);
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
                future.completeExceptionally(new RuntimeException("No reward found"));
            }
        });
        return future;
    }

    public CompletableFuture<List<RewardModel>> getRewards(@NotNull List<ObjectId> ids) {
        CompletableFuture<List<RewardModel>> future = new CompletableFuture<>();
        MongoCollection<RewardModel> collection = this.getCollection();

        List<RewardModel> list = new ArrayList<>();

        collection.find(Filters.in("_id", ids)).subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(RewardModel rewardModel) {
                list.add(rewardModel);
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

    private MongoCollection<RewardModel> getCollection() {
        return this.database.getDatabase().getCollection("reward", RewardModel.class);
    }
}
