package tv.banko.songrequest.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.helix.domain.CustomReward;
import com.github.twitch4j.helix.domain.CustomRewardList;
import com.github.twitch4j.helix.domain.UserList;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.SongRequest;
import tv.banko.songrequest.config.Config;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Twitch {

    private final SongRequest request;

    private final OAuth2Credential clientCredentials;
    private final OAuth2Credential userCredentials;
    private final TwitchClient client;
    private final TwitchAPI api;

    private String broadcasterId;

    public Twitch(@NotNull SongRequest request) {
        this.request = request;
        this.api = new TwitchAPI(this);

        Config config = request.getConfig();

        try {
            this.clientCredentials = (OAuth2Credential) this.api.getClientCredentials().get();
            this.userCredentials = new OAuth2Credential("", "");
            this.validateCredentials().get();

            this.client = TwitchClientBuilder.builder()
                    .withEnablePubSub(true)
                    .withEnableHelix(true)
                    .withDefaultAuthToken(this.clientCredentials)
                    .withEnableChat(true)
                    .withChatAccount(this.userCredentials)
                    .withClientId(config.getTwitchClientID())
                    .build();

            this.client.getChat().joinChannel(config.getTwitchChannelName());

            this.setUserID();
            this.client.getEventManager().onEvent(RewardRedeemedEvent.class, this::onRewardRedeem);
            this.client.getEventManager().onEvent(ChannelMessageEvent.class, this::onChannelMessage);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public SongRequest getRequest() {
        return request;
    }

    /**
     * Listening for channel messages in the user's channel.
     *
     * @param event The channel message event.
     */
    private void onChannelMessage(@NotNull ChannelMessageEvent event) {
        if (!event.getPermissions().contains(CommandPermission.BROADCASTER)) {
            return;
        }

        String[] args = event.getMessage().split(" ");

        if (args[0].equalsIgnoreCase("!sr-spotify")) {
            this.validateCredentials().whenCompleteAsync((aBoolean, validateThrowable) ->
                    this.request.getSpotify().getAPI().setAuthorizationFromCode(args[1]).whenCompleteAsync((o, authThrowable) -> {
                        if (authThrowable != null) {
                            authThrowable.printStackTrace();
                            this.client.getChat().sendMessage(event.getUser().getName(),
                                    "@" + event.getUser().getName() + ", Error: " + authThrowable.getClass().getSimpleName());
                            return;
                        }

                        this.client.getChat().sendMessage(event.getUser().getName(),
                                "@" + event.getUser().getName() + ", successfully connected.");
                    }));
        }
    }

    /**
     * Listening for reward redemptions in the user's channel.
     *
     * @param event The reward redeemed event.
     */
    private void onRewardRedeem(@NotNull RewardRedeemedEvent event) {
        String title = event.getRedemption().getReward().getTitle();

        this.validateCredentials().whenCompleteAsync((aBoolean, throwable) -> {
            if (title.equals(this.request.getConfig().getTwitchRedemptionAddSong())) {
                String query = event.getRedemption().getUserInput();
                this.request.getSpotify().getTrackId(query).whenCompleteAsync((trackIdObject, trackThrowable) -> {
                    if (trackThrowable != null) {
                        trackThrowable.printStackTrace();
                        this.changeRedemptionStatus(event, RedemptionStatus.CANCELED);
                        return;
                    }

                    String trackId = (String) trackIdObject;
                    this.request.getSpotify().addSongToQueue(trackId).whenCompleteAsync((o, actionThrowable) ->
                            this.action(event, actionThrowable));
                });
                return;
            }

            if (title.equals(this.request.getConfig().getTwitchRedemptionSkipSong())) {
                this.request.getSpotify().skipSong().whenCompleteAsync((o, actionThrowable) ->
                        this.action(event, actionThrowable));
                return;
            }

            if (title.equals(this.request.getConfig().getTwitchRedemptionPlayLastSong())) {
                this.request.getSpotify().playLastSong().whenCompleteAsync((o, actionThrowable) ->
                        this.action(event, actionThrowable));
                return;
            }

            if (title.equals(this.request.getConfig().getTwitchRedemptionPausePlayback())) {
                this.request.getSpotify().pausePlayback().whenCompleteAsync((o, actionThrowable) ->
                        this.action(event, actionThrowable));
                return;
            }

            if (title.equals(this.request.getConfig().getTwitchRedemptionStartPlayback())) {
                this.request.getSpotify().startPlayback().whenCompleteAsync((o, actionThrowable) ->
                        this.action(event, actionThrowable));
                return;
            }

            if (title.equals(this.request.getConfig().getTwitchRedemptionSkipTenSeconds())) {
                this.request.getSpotify().skipTenSeconds().whenCompleteAsync((o, actionThrowable) ->
                        this.action(event, actionThrowable));
                return;
            }

            if (title.equals(this.request.getConfig().getTwitchRedemptionPlayLastTenSeconds())) {
                this.request.getSpotify().playLastTenSeconds().whenCompleteAsync((o, actionThrowable) ->
                        this.action(event, actionThrowable));
            }
        });
    }

    /**
     * Gets the user id of the broadcaster.
     */
    private void setUserID() {
        new Thread(() -> {
            Config config = this.request.getConfig();

            UserList list = this.client.getHelix().getUsers(this.userCredentials.getAccessToken(), null,
                    Collections.singletonList(config.getTwitchChannelName().toLowerCase())).execute();

            list.getUsers().forEach(user -> {
                this.broadcasterId = user.getId();
                this.client.getPubSub().listenForChannelPointsRedemptionEvents(this.clientCredentials, user.getId());
            });

            this.createRewards();
        }).start();
    }

    /**
     * Create the rewards if they do not exist.
     * <br></br>
     * <b>WARNING:</b> If the reward already exists, but it was not created by the client, the execution will fail.
     * Remove the existing reward in order to ensure the perfect workflow.
     */
    private void createRewards() {
        Config config = this.request.getConfig();

        CustomRewardList rewardList = this.client.getHelix().getCustomRewards(this.userCredentials.getAccessToken(), this.broadcasterId,
                null, true).execute();

        String addSong = config.getTwitchRedemptionAddSong();

        if (!Objects.equals(addSong, "") && rewardList.getRewards().stream().noneMatch(reward -> reward.getTitle().equals(addSong))) {
            this.client.getHelix().createCustomReward(this.userCredentials.getAccessToken(), this.broadcasterId,
                    new CustomReward().withCost(1).withIsEnabled(false)
                            .withIsUserInputRequired(true)
                            .withTitle(addSong)
                            .withBroadcasterId(this.broadcasterId)).execute();
        }

        String skipSong = config.getTwitchRedemptionSkipSong();

        if (!Objects.equals(skipSong, "") && rewardList.getRewards().stream().noneMatch(reward -> reward.getTitle().equals(skipSong))) {
            this.client.getHelix().createCustomReward(this.userCredentials.getAccessToken(), this.broadcasterId,
                    new CustomReward().withCost(1).withIsEnabled(false)
                            .withTitle(skipSong)
                            .withBroadcasterId(this.broadcasterId)).execute();
        }

        String playLastSong = config.getTwitchRedemptionPlayLastSong();

        if (!Objects.equals(playLastSong, "") && rewardList.getRewards().stream().noneMatch(reward -> reward.getTitle().equals(playLastSong))) {
            this.client.getHelix().createCustomReward(this.userCredentials.getAccessToken(), this.broadcasterId,
                    new CustomReward().withCost(1).withIsEnabled(false)
                            .withTitle(playLastSong)
                            .withBroadcasterId(this.broadcasterId)).execute();
        }

        String skipTenSeconds = config.getTwitchRedemptionSkipTenSeconds();

        if (!Objects.equals(skipTenSeconds, "") && rewardList.getRewards().stream().noneMatch(reward -> reward.getTitle().equals(skipTenSeconds))) {
            this.client.getHelix().createCustomReward(this.userCredentials.getAccessToken(), this.broadcasterId,
                    new CustomReward().withCost(1).withIsEnabled(false)
                            .withTitle(skipTenSeconds)
                            .withBroadcasterId(this.broadcasterId)).execute();
        }

        String playLastTenSeconds = config.getTwitchRedemptionPlayLastTenSeconds();

        if (!Objects.equals(playLastTenSeconds, "") && rewardList.getRewards().stream().noneMatch(reward -> reward.getTitle().equals(playLastTenSeconds))) {
            this.client.getHelix().createCustomReward(this.userCredentials.getAccessToken(), this.broadcasterId,
                    new CustomReward().withCost(1).withIsEnabled(false)
                            .withTitle(playLastTenSeconds)
                            .withBroadcasterId(this.broadcasterId)).execute();
        }

        String startPlayback = config.getTwitchRedemptionStartPlayback();

        if (!Objects.equals(startPlayback, "") && rewardList.getRewards().stream().noneMatch(reward -> reward.getTitle().equals(startPlayback))) {
            this.client.getHelix().createCustomReward(this.userCredentials.getAccessToken(), this.broadcasterId,
                    new CustomReward().withCost(1).withIsEnabled(false)
                            .withTitle(startPlayback)
                            .withBroadcasterId(this.broadcasterId)).execute();
        }

        String pausePlayback = config.getTwitchRedemptionPausePlayback();

        if (!Objects.equals(pausePlayback, "") && rewardList.getRewards().stream().noneMatch(reward -> reward.getTitle().equals(pausePlayback))) {
            this.client.getHelix().createCustomReward(this.userCredentials.getAccessToken(), this.broadcasterId,
                    new CustomReward().withCost(1).withIsEnabled(false)
                            .withTitle(pausePlayback)
                            .withBroadcasterId(this.broadcasterId)).execute();
        }
    }

    /**
     * Validates the credentials for the user's account.
     * May be regenerated when the access token expires in less than 5 seconds or is already expired.
     *
     * @return
     */
    private CompletableFuture<Boolean> validateCredentials() {
        if ((this.request.getConfig().getTwitchExpiresAt() - 5000) > System.currentTimeMillis()) {
            return CompletableFuture.completedFuture(true);
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        this.api.regenerateAuthorizationCode().whenCompleteAsync((o, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }

            String[] token = ((String) o).split(" ");
            this.userCredentials.updateCredential(new OAuth2Credential(token[0], token[1]));
            future.complete(true);
        });

        return future;
    }

    /**
     * Change the redemption status.
     *
     * @param event  The event of the reward redemption
     * @param status The new status
     */
    private void changeRedemptionStatus(@NotNull RewardRedeemedEvent event, @NotNull RedemptionStatus status) {
        this.client.getHelix().updateRedemptionStatus(this.userCredentials.getAccessToken(), this.broadcasterId,
                event.getRedemption().getReward().getId(), Collections.singletonList(event.getRedemption().getId()),
                status).queue();
    }

    private void action(RewardRedeemedEvent event, Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
            this.changeRedemptionStatus(event, RedemptionStatus.CANCELED);
            return;
        }

        this.changeRedemptionStatus(event, RedemptionStatus.FULFILLED);
    }
}
