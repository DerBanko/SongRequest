package tv.banko.songrequest.spotify.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.config.Config;
import tv.banko.songrequest.spotify.SpotifyAPI;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class SpotifyAccessToken {

    private final SpotifyAPI api;

    public SpotifyAccessToken(@NotNull SpotifyAPI api) {
        this.api = api;
    }

    public CompletableFuture<Response> getAccessToken(@NotNull OkHttpClient client, @NotNull String code) {
        Config config = this.api.getSpotify().getRequest().getConfig();
        String url = MessageFormat.format("https://accounts.spotify.com/api/token?code={0}&redirect_uri={1}&grant_type={2}",
                code, config.getSpotifyRedirectURI(), "authorization_code");
        return this.execute(client, url);
    }

    public CompletableFuture<Response> regenerateToken(@NotNull OkHttpClient client, @NotNull String refreshToken) {
        String url = MessageFormat.format("https://accounts.spotify.com/api/token?refresh_token={0}&grant_type={1}",
                refreshToken, "refresh_token");
        return this.execute(client, url);
    }

    private CompletableFuture<Response> execute(@NotNull OkHttpClient client, @NotNull String url) {
        CompletableFuture<Response> future = new CompletableFuture<>();

        Config config = this.api.getSpotify().getRequest().getConfig();
        String basicAuth = Base64.getEncoder().encodeToString((config.getSpotifyClientID() + ":"
                + config.getSpotifyClientSecret()).getBytes(StandardCharsets.UTF_8));

        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create(new byte[0]));

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {
                if (response.body() == null) {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": null"));
                    return;
                }

                if (response.code() != 200) {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": " + response.body().string()));
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
