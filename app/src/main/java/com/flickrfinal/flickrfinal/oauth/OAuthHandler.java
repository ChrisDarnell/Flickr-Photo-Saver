

package com.flickrfinal.flickrfinal.oauth;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.Lists;
import com.flickrfinal.flickrfinal.FlickrDemoConstants;
import com.wuman.android.auth.oauth.OAuthHmacCredential;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import java.io.IOException;


public class OAuthHandler {
    protected static final String TAG = "OAuthHandler";

    public static final String TEMPORARY_TOKEN_REQUEST_URL = "http://m.flickr.com/services/oauth/request_token";
    public static final String AUTHORIZATION_VERIFIER_SERVER_URL = "http://m.flickr.com/services/oauth/authorize";
    public static final String TOKEN_SERVER_URL = "http://m.flickr.com/services/oauth/access_token";
    public static final String REDIRECT_URL = "http://localhost/Callback";

    protected OAuth mOauth;
    protected Credential mCredential;

    public Credential getCredential() {
        return mCredential;
    }


    public boolean getOAuthToken(FragmentActivity activity, final String userId) {
        boolean retVal = false;

        mOauth = OAuth.newInstance(activity.getApplicationContext(),
                activity.getSupportFragmentManager(),
                new ClientParametersAuthentication(FlickrDemoConstants.FLICKR_API_KEY,
                        FlickrDemoConstants.FLICKR_API_SECRET),
                AUTHORIZATION_VERIFIER_SERVER_URL,
                TOKEN_SERVER_URL,
                REDIRECT_URL,
                Lists.<String>newArrayList(),
                TEMPORARY_TOKEN_REQUEST_URL);

        try {
            mCredential = mOauth.authorize10a(
                    userId).getResult();
            if(FlickrDemoConstants.DEBUG_ENABLE) {
                Log.i(TAG, "token: " + mCredential.getAccessToken());
            }

            if(mCredential instanceof OAuthHmacCredential) {
                if(FlickrDemoConstants.DEBUG_ENABLE) {
                    Log.i(TAG, "tokenSecret: " + ((OAuthHmacCredential) mCredential).getTokenSharedSecret());
                }

                retVal = true;
            }

        } catch(IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return retVal;
    }

    public boolean removeCredential(final Context context, final String userId) {
        boolean status = false;

        SharedPreferencesCredentialStore credentialStore =
                new SharedPreferencesCredentialStore(context,
                        OAuth.CREDENTIALS_STORE_PREF_FILE, OAuth.JSON_FACTORY);

        try{
            credentialStore.delete(userId, mCredential);
            mCredential = null;
            status = true;
        } catch (IOException e) {
            if(FlickrDemoConstants.DEBUG_ENABLE) {
                Log.e(TAG, "Exception deleting credential", e);
            }
        }

        return status;
    }



    public boolean checkPreviousAuthorization(final Context context, final String userId) {
        boolean status = false;

        SharedPreferencesCredentialStore credentialStore =
                new SharedPreferencesCredentialStore(context,
                        OAuth.CREDENTIALS_STORE_PREF_FILE, OAuth.JSON_FACTORY);

        if(mCredential == null) {

            mCredential = new OAuthHmacCredential.Builder(BearerToken.authorizationHeaderAccessMethod(), "", "").build();
        }

        try{
            status = credentialStore.load(userId, mCredential);
        } catch (IOException e) {
            if(FlickrDemoConstants.DEBUG_ENABLE) {
                Log.e(TAG, "Exception loading credential", e);
            }
        }

        return status;
    }
}
