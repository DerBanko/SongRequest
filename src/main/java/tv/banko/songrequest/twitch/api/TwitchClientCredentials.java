package tv.banko.songrequest.twitch.api;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.config.Config;
import tv.banko.songrequest.twitch.TwitchAPI;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

public class TwitchClientCredentials {

    private final TwitchAPI api;

    public TwitchClientCredentials(@NotNull TwitchAPI api) {
        this.api = api;
    }

    public CompletableFuture<OAuth2Credential> execute(@NotNull OkHttpClient client) {
        CompletableFuture<OAuth2Credential> future = new CompletableFuture<>();

        Config config = this.api.getTwitch().getRequest().getConfig();
        String url = MessageFormat.format("https://id.twitch.tv/oauth2/token" +
                        "?client_id={0}&client_secret={1}&grant_type={2}",
                config.getTwitchClientID(), config.getTwitchClientSecret(), "client_credentials");

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0]));

        client.newCall(builder.build()).enqueue(new Callback() {
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
