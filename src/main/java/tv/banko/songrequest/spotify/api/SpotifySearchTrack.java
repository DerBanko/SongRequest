package tv.banko.songrequest.spotify.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

public class SpotifySearchTrack {

    public CompletableFuture<String> execute(@NotNull OkHttpClient client, @NotNull String accessToken, @NotNull String query) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Request.Builder builder = new Request.Builder()
                .url(MessageFormat.format("https://api.spotify.com/v1/search?q={0}&type={1}", query, "track"))
                .header("Authorization", accessToken)
                .post(RequestBody.create(new byte[0]));

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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
                    JsonObject tracks = object.getAsJsonObject("tracks");
                    JsonArray items = tracks.getAsJsonArray("items");

                    if (items.size() == 0) {
                        future.completeExceptionally(new NullPointerException("No tracks found"));
                        return;
                    }

                    JsonObject item = items.get(0).getAsJsonObject();
                    String id = item.get("id").getAsString();
                    future.complete("spotify:track:" + id);
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }
}
