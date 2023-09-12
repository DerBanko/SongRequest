package tv.banko.songrequest.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Config {

    private final File file;
    private JsonObject object;

    public Config() {
        this.file = new File("config/config.json");

        if (!file.exists()) {

            File dir = new File("config");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            try {
                file.createNewFile();
                try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.json")) {
                    if (stream == null) {
                        throw new FileNotFoundException("Resource 'config.json' not existing");
                    }

                    String data = new String(stream.readAllBytes());

                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                    outputStream.close();

                    this.object = JsonParser.parseString(data).getAsJsonObject();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        try (FileInputStream stream = new FileInputStream(file)) {
            this.object = JsonParser.parseString(new String(stream.readAllBytes())).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the spotify access token from the config.
     *
     * @return The spotify access token.
     */
    public String getSpotifyAccessToken() {
        return this.object.getAsJsonObject("spotify")
                .get("token")
                .getAsString();
    }

    /**
     * Get the spotify refresh token from the config.
     *
     * @return The spotify refresh token.
     */
    public String getSpotifyRefreshToken() {
        return this.object.getAsJsonObject("spotify")
                .get("refreshToken")
                .getAsString();
    }

    /**
     * Get the expiration timestamp of the spotify access token from the config.
     *
     * @return The expiration timestamp.
     */
    public long getSpotifyExpiresAt() {
        return this.object.getAsJsonObject("spotify")
                .get("expiresAt")
                .getAsLong();
    }

    /**
     * Get the spotify client id from the config.
     *
     * @return The spotify client id.
     */
    public String getSpotifyClientID() {
        return this.object.getAsJsonObject("spotify")
                .get("clientId")
                .getAsString();
    }

    /**
     * Get the spotify client secret from the config.
     *
     * @return The spotify client secret.
     */
    public String getSpotifyClientSecret() {
        return this.object.getAsJsonObject("spotify")
                .get("clientSecret")
                .getAsString();
    }

    /**
     * Get the spotify redirect uri from the config.
     *
     * @return The spotify redirect uri.
     */
    public String getSpotifyRedirectURI() {
        return this.object.getAsJsonObject("spotify")
                .get("redirectURI")
                .getAsString();
    }

    /**
     * Get the twitch access token from the config.
     *
     * @return The twitch access token.
     */
    public String getTwitchAccessToken() {
        return this.object.getAsJsonObject("twitch")
                .get("token")
                .getAsString();
    }

    /**
     * Get the twitch refresh token from the config.
     *
     * @return The twitch refresh token.
     */
    public String getTwitchRefreshToken() {
        return this.object.getAsJsonObject("twitch")
                .get("refreshToken")
                .getAsString();
    }

    /**
     * Get the expiration timestamp of the twitch access token from the config.
     *
     * @return The expiration timestamp of the twitch access token.
     */
    public Integer getTwitchExpiresAt() {
        return this.object.getAsJsonObject("twitch")
                .get("expiresAt")
                .getAsInt();
    }

    /**
     * Get the twitch client id from the config.
     *
     * @return The twitch client id.
     */
    public String getTwitchClientID() {
        return this.object.getAsJsonObject("twitch")
                .get("clientId")
                .getAsString();
    }

    /**
     * Get the twitch client secret from the config.
     *
     * @return The twitch client secret.
     */
    public String getTwitchClientSecret() {
        return this.object.getAsJsonObject("twitch")
                .get("clientSecret")
                .getAsString();
    }

    /**
     * Get the twitch channel name from the config.
     *
     * @return The twitch channel name.
     */
    public String getTwitchChannelName() {
        return this.object.getAsJsonObject("twitch")
                .get("channel")
                .getAsString();
    }

    /**
     * Get the name of the add song redemption from the config.
     *
     * @return The name of the add song redemption.
     */
    public String getTwitchRedemptionAddSong() {
        return this.object.getAsJsonObject("twitch")
                .getAsJsonObject("redemption")
                .get("addSong")
                .getAsString();
    }

    /**
     * Get the name of the skip song redemption from the config.
     *
     * @return The name of the skip song redemption.
     */
    public String getTwitchRedemptionSkipSong() {
        return this.object.getAsJsonObject("twitch")
                .getAsJsonObject("redemption")
                .get("skipSong")
                .getAsString();
    }

    /**
     * Get the name of the play last song redemption from the config.
     *
     * @return The name of the skip song redemption.
     */
    public String getTwitchRedemptionPlayLastSong() {
        return this.object.getAsJsonObject("twitch")
                .getAsJsonObject("redemption")
                .get("playLastSong")
                .getAsString();
    }

    /**
     * Get the name of the start playback redemption from the config.
     *
     * @return The name of the skip song redemption.
     */
    public String getTwitchRedemptionStartPlayback() {
        return this.object.getAsJsonObject("twitch")
                .getAsJsonObject("redemption")
                .get("startPlayback")
                .getAsString();
    }

    /**
     * Get the name of the pause playback redemption from the config.
     *
     * @return The name of the skip song redemption.
     */
    public String getTwitchRedemptionPausePlayback() {
        return this.object.getAsJsonObject("twitch")
                .getAsJsonObject("redemption")
                .get("pausePlayback")
                .getAsString();
    }

    /**
     * Get the name of the skip ten seconds redemption from the config.
     *
     * @return The name of the skip song redemption.
     */
    public String getTwitchRedemptionSkipTenSeconds() {
        return this.object.getAsJsonObject("twitch")
                .getAsJsonObject("redemption")
                .get("skipTenSeconds")
                .getAsString();
    }

    /**
     * Get the name of the play last ten seconds redemption from the config.
     *
     * @return The name of the skip song redemption.
     */
    public String getTwitchRedemptionPlayLastTenSeconds() {
        return this.object.getAsJsonObject("twitch")
                .getAsJsonObject("redemption")
                .get("playLastTenSeconds")
                .getAsString();
    }

    /**
     * Set a string value in the config.
     *
     * @param key   The key in the config.
     * @param value The value.
     */
    public void setValue(@NotNull String key, @Nullable String value) {
        this.setValue(key, (jsonObject, s) -> jsonObject.addProperty(s, value));
    }

    /**
     * Set an integer value in the config.
     *
     * @param key   The key in the config.
     * @param value The value.
     */
    public void setValue(@NotNull String key, @Nullable Integer value) {
        this.setValue(key, (jsonObject, s) -> jsonObject.addProperty(s, value));
    }

    /**
     * Set a long value in the config.
     *
     * @param key   The key in the config.
     * @param value The value.
     */
    public void setValue(@NotNull String key, @Nullable Long value) {
        this.setValue(key, (jsonObject, s) -> jsonObject.addProperty(s, value));
    }

    /**
     * Set a value in the config.
     *
     * @param key      The key in the config.
     * @param consumer The adding consumer.
     */
    private void setValue(@NotNull String key, @NotNull BiConsumer<JsonObject, String> consumer) {
        List<JsonObject> objects = new ArrayList<>();

        objects.add(this.object);

        String[] keyArray = key.split("\\.");
        for (int i = 0; i < keyArray.length; i++) {
            JsonObject jsonObject = objects.get(i);
            JsonElement element = jsonObject.get(keyArray[i]);

            if (element.isJsonObject()) {
                objects.add(element.getAsJsonObject());
                continue;
            }

            consumer.accept(jsonObject, keyArray[i]);
        }

        for (int i = keyArray.length - 1; i >= 0; i--) {
            if (i == 0) {
                this.object = objects.get(i);
                break;
            }

            objects.get(i - 1).add(keyArray[i - 1], objects.get(i));
        }

        this.save();
    }

    /**
     * Save the config.
     */
    private void save() {
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            FileWriter writer = new FileWriter(this.file, false);
            writer.write(this.object.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
