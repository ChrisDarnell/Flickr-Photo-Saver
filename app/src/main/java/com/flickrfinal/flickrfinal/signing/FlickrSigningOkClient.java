

package com.flickrfinal.flickrfinal.signing;

import android.util.Log;

import com.flickrfinal.flickrfinal.FlickrDemoConstants;

import java.io.IOException;

import retrofit.client.Request;
import retrofit.client.Response;
import se.akerfeldt.signpost.retrofit.RetrofitHttpOAuthConsumer;
import se.akerfeldt.signpost.retrofit.SigningOkClient;


public class FlickrSigningOkClient extends SigningOkClient {
    protected final String mTokenSecret;

    public FlickrSigningOkClient(RetrofitHttpOAuthConsumer consumer) {
        super(consumer);
        mTokenSecret = consumer.getTokenSecret();
    }

    @Override

    public Response execute(Request request) throws IOException {
        String modifiedUrl = RequestSigner.addStaticParams(request.getUrl());
        modifiedUrl = RequestSigner.signRequest(modifiedUrl, mTokenSecret);
        final Request modifiedRequest = new Request(request.getMethod(), modifiedUrl, request.getHeaders(), request.getBody());

        if (FlickrDemoConstants.DEBUG_ENABLE_VERBOSE) {
            Log.i("FlickrSigningOkClient", "final signed request: " + modifiedUrl);
        }

        return super.execute(modifiedRequest);
    }
}
