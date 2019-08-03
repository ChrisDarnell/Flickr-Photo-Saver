

package com.flickrfinal.flickrfinal.retrofit;

import com.flickrfinal.flickrfinal.FlickrDemoConstants;
import com.flickrfinal.flickrfinal.signing.FlickrSigningOkClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import se.akerfeldt.signpost.retrofit.RetrofitHttpOAuthConsumer;

public class ServiceGenerator {
    public static final String FLICKR_URL_BASE = "https://api.flickr.com";


    private ServiceGenerator() {
    }


    public static <S> S createServiceSigned(Class<S> serviceClass, String baseUrl, final String token, final String tokenSecret) {
        RetrofitHttpOAuthConsumer oAuthConsumer = new RetrofitHttpOAuthConsumer(FlickrDemoConstants.FLICKR_API_KEY, FlickrDemoConstants.FLICKR_API_SECRET);
        oAuthConsumer.setTokenWithSecret(token, tokenSecret);

        OkClient client = new FlickrSigningOkClient(oAuthConsumer);

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(baseUrl)
                .setClient(client);

        RestAdapter adapter = builder.build();

        if(FlickrDemoConstants.DEBUG_ENABLE_VERBOSE) {
            adapter.setLogLevel(RestAdapter.LogLevel.FULL);
        }

        return adapter.create(serviceClass);
    }
}