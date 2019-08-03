

package com.flickrfinal.flickrfinal.display;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.flickrfinal.flickrfinal.FlickrImageUrl;
import com.flickrfinal.flickrfinal.R;
import com.flickrfinal.flickrfinal.retrofit.response.photosets.Photoset;
import com.flickrfinal.flickrfinal.retrofit.response.photosets.Photosets;
import com.squareup.picasso.Picasso;

import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class PhotosetAdapter extends BaseAdapter {
    protected final Context context;

    protected final List<Photoset> mPhotosets;

    public PhotosetAdapter(Context context, final Photosets photosets) {
        this.context = context;

        mPhotosets = photosets.getPhotosets().getPhotoset();
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        Photoset photoset = mPhotosets.get(position);

        final String url = FlickrImageUrl.getUrl(photoset);

        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.ic_action_picture)
                .error(R.drawable.ic_action_warning)
                .fit()
                .tag(context)
                .into(view);

        return view;
    }

    @Override public int getCount() {
        return mPhotosets.size();
    }

    @Override public Photoset getItem(int position) {
     return mPhotosets.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }
}