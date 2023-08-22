package tv.banko.songrequest.spotify.api;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

public class SpotifyPlayerAction {

    public CompletableFuture<Boolean> addSongToQueue(@NotNull OkHttpClient client, @NotNull String accessToken, @NotNull String spotifyTrackId) {
        return this.execute(client, accessToken, MessageFormat.format("https://api.spotify.com/v1/me/player/queue?uri={0}", spotifyTrackId));
    }

    public CompletableFuture<Boolean> skipSong(@NotNull OkHttpClient client, @NotNull String accessToken) {
        return this.execute(client, accessToken, "https://api.spotify.com/v1/me/player/next");
    }

    private CompletableFuture<Boolean> execute(@NotNull OkHttpClient client, @NotNull String accessToken, @NotNull String url) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Authorization", accessToken)
                .post(RequestBody.create(new byte[0]));

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 204) {
                    assert response.body() != null;
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": " + response.body().string()));
                    return;
                }

                future.complete(true);
            }
        });
        return future;
    }
}
