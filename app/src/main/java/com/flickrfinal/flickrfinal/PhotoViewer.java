

package com.flickrfinal.flickrfinal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.flickrfinal.flickrfinal.retrofit.response.StatusResponse;
import com.flickrfinal.flickrfinal.retrofit.response.photoinfo.Photo;
import com.flickrfinal.flickrfinal.retrofit.response.photoinfo.PhotoInfo;
import com.squareup.picasso.Picasso;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Show a single photo
 */
public class PhotoViewer extends Activity {
    public static final String INTENT_EXTRA_PHOTO_URL       = "INTENT_EXTRA_PHOTO_URL";
    public static final String INTENT_EXTRA_PHOTO_ID        = "INTENT_EXTRA_PHOTOID";
    public static final String INTENT_EXTRA_PHOTO_SECRET    = "INTENT_EXTRA_PHOTO_SECRET";
    public static final String INTENT_EXTRA_HIDE_FAVORITE   = "INTENT_EXTRA_HIDE_FAVORITE";

    protected String mPhotoId;
    protected String mPhotoSecret;

    protected PhotoInfo mPhotoInfo;

    protected boolean mHideFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme here if needed
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_viewer);

        final Intent intent = getIntent();
        if(null != intent) {
            final String photoUrl       = intent.getStringExtra(INTENT_EXTRA_PHOTO_URL);

            mPhotoId                    = intent.getStringExtra(INTENT_EXTRA_PHOTO_ID);
            mPhotoSecret                = intent.getStringExtra(INTENT_EXTRA_PHOTO_SECRET);

            mHideFavorite = intent.getBooleanExtra(INTENT_EXTRA_HIDE_FAVORITE, FlickrDemoConstants.DEFAULT_BOOLEAN);

            final ImageView imageView = (ImageView) findViewById(R.id.photo_viewer_image);

            if(null != photoUrl) {
                Picasso.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_action_picture)
                        .error(R.drawable.ic_action_warning)
                        .tag(this)
                        .into(imageView);
            }
        }

        // Load info,
        FlickrDemo.getFlickrClient().getPhotoInfo(mPhotoId, mPhotoSecret, photoInfoCallback);
    }

    public void onClickInfo(final View v) {
        if (FlickrDemoConstants.DEBUG_ENABLE) {
            Log.i("onClickInfo", "onClickInfo");
        }

        Photo photo = mPhotoInfo.getPhoto();

        // User data for display
        SpannableStringBuilder builder = new SpannableStringBuilder();

        StaticUtil.addPairToSpannable(builder, "Image metadata for", photo.getTitle().getContent());
        StaticUtil.addPairToSpannable(builder, "Owner", photo.getOwner().getUsername());

        if(!mHideFavorite) { // can't favorite your own photo, so favorite status wouldn't be needed
            StaticUtil.addPairToSpannable(builder, "Is your favorite", Boolean.toString(photo.getIsfavorite() != 0));
        }

        StaticUtil.addPairToSpannable(builder, "Description", photo.getDescription().getContent());

        StaticUtil.showOkAlert(this, new SpannableString(builder));
    }


    public void onClickFavorite(final View v) {
        // TODO Toggle favorite status
        if (FlickrDemoConstants.DEBUG_ENABLE) {
            Log.i("onClickFavorite", "onClickFavorite");
        }

        if(mPhotoInfo.getPhoto().getIsfavorite() == FlickrDemoConstants.FAVORITE_NO) {
            FlickrDemo.getFlickrClient().addFavorite(mPhotoId, favoriteCallback);
        } else {
            FlickrDemo.getFlickrClient().removeFavorite(mPhotoId, favoriteCallback);
        }
    }



    protected retrofit.Callback photoInfoCallback = new retrofit.Callback() {
        @Override
        public void success(Object o, Response response) {
            mPhotoInfo = (PhotoInfo)o;

            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.i("photoInfoCallback", "Favorites: " + mPhotoInfo.getPhoto().getIsfavorite());
            }


            final ImageView imageView = (ImageView) findViewById(R.id.photo_viewer_favorite);
            if(null != imageView) {
                if(mHideFavorite) {
                    imageView.setVisibility(View.INVISIBLE);
                } else {
                    if (mPhotoInfo.getPhoto().getIsfavorite() == FlickrDemoConstants.FAVORITE_NO) {
                        imageView.setImageResource(R.drawable.ic_action_star_0);
                    } else {
                        imageView.setImageResource(R.drawable.ic_action_star_10);
                    }
                }
            }

            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.i("photoInfoCallback", "Received photo info");
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.e("photoInfoCallback", "RetrofitError" + retrofitError.getMessage());
            }
        }
    };


    protected retrofit.Callback favoriteCallback = new retrofit.Callback() {
        @Override
        public void success(Object o, Response response) {
            StatusResponse status = (StatusResponse)o;

            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.i("favoriteCallback", "Received response for favorite add/remove: " + status.getStat());
            }

            if(status.getStat().equals(FlickrDemoConstants.FLICKR_RESPONSE_OK)) {
                // If post was ok, then data changed.  Reload it.
                FlickrDemo.getFlickrClient().getPhotoInfo(mPhotoId, mPhotoSecret, photoInfoCallback);
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            if (FlickrDemoConstants.DEBUG_ENABLE) {
                Log.e("photoInfoCallback", "RetrofitError" + retrofitError.getMessage());
            }
        }
    };
}
