package tv.banko.songrequest.twitch.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.config.Config;
import tv.banko.songrequest.twitch.TwitchAPI;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

public class TwitchAccessToken {

    private final TwitchAPI api;

    public TwitchAccessToken(@NotNull TwitchAPI api) {
        this.api = api;
    }

    public CompletableFuture<Response> getAccessToken(@NotNull OkHttpClient client, @NotNull String code) {
        Config config = this.api.getTwitch().getRequest().getConfig();

        String url = MessageFormat.format("https://id.twitch.tv/oauth2/token?code={0}&grant_type={1}&client_id={2}&client_secret={3}",
                code, "authorization_code", config.getTwitchClientID(), config.getTwitchClientSecret());
        return this.execute(client, url);
    }

    public CompletableFuture<Response> regenerateAccessToken(@NotNull OkHttpClient client, @NotNull String refreshToken) {
        Config config = this.api.getTwitch().getRequest().getConfig();

        String url = MessageFormat.format("https://id.twitch.tv/oauth2/token?refresh_token={0}&grant_type={1}&client_id={2}&client_secret={3}",
                refreshToken, "refresh_token", config.getTwitchClientID(), config.getTwitchClientSecret());
        return this.execute(client, url);
    }

    private CompletableFuture<Response> execute(@NotNull OkHttpClient client, @NotNull String url) {
        CompletableFuture<Response> future = new CompletableFuture<>();

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0]));

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) {
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
                    future.complete(new Response("Bearer " + object.get("access_token").getAsString(),
                            object.get("refresh_token").getAsString(),
                            System.currentTimeMillis() + (object.get("expires_in").getAsInt() * 1000L)));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    public record Response(@NotNull String accessToken, @NotNull String refreshToken, @NotNull Long expiresAt) {

    }
}
