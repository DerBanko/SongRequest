#!/bin/bash
echo "Welcome to the setup script for the standalone song requests for your Twitch channel."
echo "You need a Twitch application. To create one, visit https://dev.twitch.tv/console/apps/create and create a Twitch application."
echo "You can use https://localhost:8080 as your redirect url and Chat bot as the category."
echo "Please enter the CLIENT ID of your Twitch application:"
read twitch_client_id
echo "Please enter the CLIENT SECRET of your Twitch application:"
read twitch_client_secret
echo "Please enter the REDIRECT URL of your Twitch application:"
read twitch_redirect_url
echo "You can close the Twitch application window now."
echo "Lets continue with your Spotify application. To create one, visit (https://developer.spotify.com/dashboard/create and create a Spotify application."
echo "You can use https://localhost:8080 as your redirect url."
echo "Please enter the CLIENT ID of your Spotify application:"
read spotify_client_id
echo "Please enter the CLIENT SECRET of your Spotify application:"
read spotify_client_secret
echo "Please enter the REDIRECT URL of your Spotify application:"
read spotify_redirect_url
echo "Spotify application complete."
echo "Copy the following url and paste it in your browser: https://id.twitch.tv/oauth2/authorize?scope=channel:read:redemptions+channel:manage:redemptions+chat:edit+chat:read&redirect_uri=${twitch_redirect_url}&client_id=${twitch_client_id}&response_type=code"
echo "Paste the code from the url in here ($twitch_redirect_url?code=[CODE]&scope=...):"
read twitch_code
twitch_response=$(curl -X POST https://id.twitch.tv/oauth2/token?client_id=${twitch_client_id}&client_secret=${twitch_client_secret}&code=${twitch_code}&grant_type=authorization_code&redirect_uri=${twitch_redirect_url})
echo "twitch_response=$twitch_response https://id.twitch.tv/oauth2/token?client_id=${twitch_client_id}&client_secret=${twitch_client_secret}&code=${twitch_code}&grant_type=authorization_code&redirect_uri=${twitch_redirect_url}"
IFS='"access_token": "' read -a twitch_access_token_array_1 <<< "$twitch_response"
IFS='",' read -a twitch_access_token_array_2 <<< "${twitch_access_token_array_1[1]}"
twitch_access_token="${twitch_access_token_array_2[0]}"
S='"refresh_token": "' read -r -a twitch_refresh_token_array_1 <<< "$twitch_response"
IFS='",' read -a twitch_refresh_token_array_2 <<< "${twitch_refresh_token_array_1[1]}"
twitch_refresh_token="${twitch_refresh_token_array_2[0]}"
echo "access_token=$twitch_access_token refresh_token=$twitch_refresh_token"