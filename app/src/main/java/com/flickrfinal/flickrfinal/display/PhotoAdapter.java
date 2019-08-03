

package com.flickrfinal.flickrfinal.display;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.flickrfinal.flickrfinal.FlickrImageUrl;
import com.flickrfinal.flickrfinal.R;
import com.flickrfinal.flickrfinal.retrofit.response.PhotoCommon;
import com.squareup.picasso.Picasso;

import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class PhotoAdapter extends BaseAdapter {
    protected final Context context;

    protected final List<PhotoCommon> mPhotos;

    public PhotoAdapter(Context context, final List<PhotoCommon> photos) {
        this.context = context;

        mPhotos = photos;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        PhotoCommon photo = mPhotos.get(position);

        final String url = FlickrImageUrl.getUrl(photo, FlickrImageUrl.USE_THUMBNAIL_SIZE);

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
        return mPhotos.size();
    }

    @Override public PhotoCommon getItem(int position) {
     return mPhotos.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }
}