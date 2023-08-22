package tv.banko.songrequest.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.twitch.api.TwitchClientCredentials;
import tv.banko.songrequest.twitch.api.TwitchAccessToken;

import java.util.concurrent.CompletableFuture;

public class TwitchAPI {

    private final Twitch twitch;
    private final OkHttpClient client;

    private final TwitchAccessToken accessToken;
    private final TwitchClientCredentials clientCredentials;

    public TwitchAPI(@NotNull Twitch twitch) {
        this.twitch = twitch;
        this.client = new OkHttpClient();
        this.accessToken = new TwitchAccessToken(this);
        this.clientCredentials = new TwitchClientCredentials(this);
    }

    public Twitch getTwitch() {
        return twitch;
    }

    /**
     * Regenerate the authentication token for the Twitch user account.
     *
     * @return A completable future which contains the access token, refresh token and expiry date when the execution was successful.
     */
    public CompletableFuture<TwitchAccessToken.Response> regenerateAuthorizationCode(@NotNull String refreshToken) {
        return this.accessToken.regenerateAccessToken(client, refreshToken);
    }

    /**
     * Get the client credentials by using the client id and client secret.
     *
     * @return A completable future which contains a OAuth2Credential object containing the credentials when the execution was successful.
     */
    public CompletableFuture<OAuth2Credential> getClientCredentials() {
        return this.clientCredentials.execute(client);
    }
}
