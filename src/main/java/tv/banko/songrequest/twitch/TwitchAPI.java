package tv.banko.songrequest.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.config.Config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class TwitchAPI {

    private final Twitch twitch;
    private final OkHttpClient client;

    public TwitchAPI(@NotNull Twitch twitch) {
        this.twitch = twitch;
        this.client = new OkHttpClient();
    }

    /**
     * Regenerate the authentication token for the Twitch user account.
     *
     * @return A completable future which contains the authentication token when the execution was successful.
     */
    public CompletableFuture<Object> regenerateAuthorizationCode() {
        CompletableFuture<Object> future = new CompletableFuture<>();

        Config config = this.twitch.getRequest().getConfig();
        String url = MessageFormat.format("https://id.twitch.tv/oauth2/token" +
                        "?refresh_token={0}&grant_type={1}&client_id={2}&client_secret={3}",
                config.getTwitchRefreshToken(), "refresh_token", config.getTwitchClientID(), config.getTwitchClientSecret());
        String basicAuth = Base64.getEncoder().encodeToString((config.getSpotifyClientID() + ":"
                + config.getSpotifyClientSecret()).getBytes(StandardCharsets.UTF_8));

        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Authorization", "Basic " + basicAuth)
                .post(RequestBody.create(new byte[0]));

        this.client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() != 200) {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code()));
                    return;
                }

                if (response.body() == null) {
                    future.completeExceptionally(new NullPointerException("Response is null"));
                    return;
                }

                try {
                    JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();

                    config.setValue("twitch.token", "Bearer " + object.get("access_token").getAsString());
                    config.setValue("twitch.refreshToken", object.get("refresh_token").getAsString());
                    config.setValue("twitch.expiresAt", System.currentTimeMillis()
                            + (object.get("expires_in").getAsInt() * 1000L));

                    future.complete(config.getTwitchAccessToken());
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    /**
     * Get the client credentials by using the client id and client secret.
     *
     * @return A completable future which contains a OAuth2Credential object containing the credentials when the execution was successful.
     */
    public CompletableFuture<Object> getClientCredentials() {
        CompletableFuture<Object> future = new CompletableFuture<>();

        Config config = this.twitch.getRequest().getConfig();
        String url = MessageFormat.format("https://id.twitch.tv/oauth2/token" +
                        "?client_id={0}&client_secret={1}&grant_type={2}",
                config.getTwitchClientID(), config.getTwitchClientSecret(), "client_credentials");
        String basicAuth = Base64.getEncoder().encodeToString((config.getSpotifyClientID() + ":"
                + config.getSpotifyClientSecret()).getBytes(StandardCharsets.UTF_8));

        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Authorization", "Basic " + basicAuth)
                .post(RequestBody.create(new byte[0]));

        this.client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() != 200) {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code()));
                    return;
                }

                if (response.body() == null) {
                    future.completeExceptionally(new NullPointerException("Response is null"));
                    return;
                }

                try {
                    JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    future.complete(new OAuth2Credential("Bearer", object.get("access_token").getAsString()));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }


}
