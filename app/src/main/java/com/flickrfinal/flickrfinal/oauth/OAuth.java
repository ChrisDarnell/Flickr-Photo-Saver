

package com.flickrfinal.flickrfinal.oauth;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.wuman.android.auth.AuthorizationDialogController;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.OAuthManager.OAuthCallback;
import com.wuman.android.auth.OAuthManager.OAuthFuture;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import java.io.IOException;
import java.util.List;


public class OAuth {

    public static final String CREDENTIALS_STORE_PREF_FILE = "oauth";

    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    protected static final boolean USE_FULLSCREEN = true;

    private final OAuthManager manager;

    public static OAuth newInstance(Context context,
            FragmentManager fragmentManager,
            ClientParametersAuthentication client,
            String authorizationRequestUrl,
            String tokenServerUrl,
            final String redirectUri,
            List<String> scopes,
            String temporaryTokenRequestUrl) {
        Preconditions.checkNotNull(client.getClientId());

        SharedPreferencesCredentialStore credentialStore =
                new SharedPreferencesCredentialStore(context,
                        CREDENTIALS_STORE_PREF_FILE, JSON_FACTORY);
        // authorization flow
        AuthorizationFlow.Builder flowBuilder = new AuthorizationFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(tokenServerUrl),
                client,
                client.getClientId(),
                authorizationRequestUrl)
                .setScopes(scopes)
                .setCredentialStore(credentialStore);
        // set temporary token request url for 1.0a flow
        if (!TextUtils.isEmpty(temporaryTokenRequestUrl)) {
            flowBuilder.setTemporaryTokenRequestUrl(temporaryTokenRequestUrl);
        }
        AuthorizationFlow flow = flowBuilder.build();

        AuthorizationDialogController controller =
                new DialogFragmentController(fragmentManager, USE_FULLSCREEN) {
                    @Override
                    public String getRedirectUri() throws IOException {
                        return redirectUri;
                    }

                    @Override
                    public boolean isJavascriptEnabledForWebView() {
                        return true;
                    }

                    @Override
                    public boolean disableWebViewCache() {
                        return false;
                    }

                    @Override
                    public boolean removePreviousCookie() {
                        return false;
                    }
                };
        return new OAuth(flow, controller);
    }

    private OAuth(AuthorizationFlow flow, AuthorizationDialogController controller) {
        Preconditions.checkNotNull(flow);
        Preconditions.checkNotNull(controller);
        this.manager = new OAuthManager(flow, controller);
    }

    public OAuthFuture<Boolean> deleteCredential(String userId) {
        return deleteCredential(userId, null);
    }

    public OAuthFuture<Boolean> deleteCredential(String userId, OAuthCallback<Boolean> callback) {
        return deleteCredential(userId, callback, null);
    }

    public OAuthFuture<Boolean> deleteCredential(String userId, OAuthCallback<Boolean> callback,
            Handler handler) {
        return manager.deleteCredential(userId, callback, handler);
    }


    public OAuthFuture<Credential> authorize10a(String userId) {
        return authorize10a(userId, null);
    }

    public OAuthFuture<Credential> authorize10a(String userId, OAuthCallback<Credential> callback) {
        return authorize10a(userId, callback, null);
    }

    public OAuthFuture<Credential> authorize10a(String userId,
            OAuthCallback<Credential> callback, Handler handler) {
        return manager.authorize10a(userId, callback, handler);
    }
}