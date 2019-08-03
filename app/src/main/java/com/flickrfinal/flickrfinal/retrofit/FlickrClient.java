

package com.flickrfinal.flickrfinal.retrofit;

import com.flickrfinal.flickrfinal.retrofit.response.StatusResponse;
import com.flickrfinal.flickrfinal.retrofit.response.TokenVerify;
import com.flickrfinal.flickrfinal.retrofit.response.favorites.FavoritesResponse;
import com.flickrfinal.flickrfinal.retrofit.response.interestingness.Interestingness;
import com.flickrfinal.flickrfinal.retrofit.response.photoinfo.PhotoInfo;
import com.flickrfinal.flickrfinal.retrofit.response.photoset.Photoset;
import com.flickrfinal.flickrfinal.retrofit.response.photosets.Photosets;
import com.flickrfinal.flickrfinal.retrofit.response.userinfo.UserData;
import com.flickrfinal.flickrfinal.retrofit.response.username.UsernameResponse;
import com.flickrfinal.flickrfinal.retrofit.response.validate.TokenTest;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;


public interface FlickrClient {

    String FLICKR_URL_BASE = "https://api.flickr.com";


    @GET("/services/rest/?method=flickr.auth.oauth.checkToken")
    TokenVerify validateToken(@Query("oauth_token") String oauth_token);

    @GET("/services/rest/?method=flickr.auth.oauth.checkToken")
    void validateToken(@Query("oauth_token") String oauth_token, Callback<TokenVerify> cb);


    /*
     * Retrieve userId of logged in user
     */
    @GET("/services/rest/?method=flickr.test.login")
    void testToken(Callback<TokenTest> cb);

    @GET("/services/rest/?method=flickr.people.getInfo")
    UserData getUserData(@Query("user_id") String userID);

    @GET("/services/rest/?method=flickr.people.getInfo")
    void getUserData(@Query("user_id") String userID, Callback<UserData> cb);

    @GET("/services/rest/?method=flickr.people.findByUsername")
    void getUserIdFromName(@Query("username") String username, Callback<UsernameResponse> cb);


    @GET("/services/rest/?method=flickr.photosets.getPhotos")
    Photoset getPhotoset(@Query("photoset_id") String photosetId,  @Query("user_id") String userId);

    @GET("/services/rest/?method=flickr.photosets.getPhotos")
    void getPhotoset(@Query("photoset_id") String photosetId,  @Query("user_id") String userId, Callback<Photoset> cb);

    @GET("/services/rest/?method=flickr.photosets.getList")
    void getPhotosetList(@Query("user_id") String userId, Callback<Photosets> cb);


    @GET("/services/rest/?method=flickr.interestingness.getList")
    void getInterestingness(@Query("per_page") String perPage,  @Query("page") String page, Callback<Interestingness> cb);



    @GET("/services/rest/?method=flickr.photos.getInfo")
    void getPhotoInfo(@Query("photo_id") String photoId,  @Query("secret") String secret, Callback<PhotoInfo> cb);



    @GET("/services/rest/?method=flickr.favorites.getList")
    void getFavorites(@Query("user_id") String userId, Callback<FavoritesResponse> cb);


    @GET("/services/rest/?method=flickr.favorites.add")
    void addFavorite(@Query("photo_id") String photoId, Callback<StatusResponse> cb);

    @GET("/services/rest/?method=flickr.favorites.remove")
    void removeFavorite(@Query("photo_id") String photoId, Callback<StatusResponse> cb);
}

