package com.example.fhuang.myproj0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Felix Huang on 7/11/2015.
 */
public class ArtistPhotoAdapter extends ArrayAdapter<ArtistPhoto> {
    public ArtistPhotoAdapter(Context context, List<ArtistPhoto> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    class ViewHolder {
        public TextView artistName;
        public ImageView artistImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get item data
        ArtistPhoto photo = getItem(position);
        ViewHolder holder;
        // check if we're using a recycled view, if not we need to inflate
        if (convertView == null) { // need to create a new view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
            // parent : current container; false : not to attach this item view to container yet
            holder = new ViewHolder();
            holder.artistName = (TextView) convertView.findViewById(R.id.tvName);
            holder.artistImage = (ImageView) convertView.findViewById(R.id.ivPhoto);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        // look up views for populating the data (image, caption)
        ImageView ivPhoto = (ImageView) holder.artistImage;
        TextView tvName = (TextView) holder.artistName;

        // insert the model data into each item views
        // clear image view
        ivPhoto.setImageResource(0);

        // insert the image using picasso; send asynch out which runs in background
        if (photo.imageUrl != null) { // to do : to check more for valid url
            Picasso.with(getContext()).load(photo.imageUrl).into(ivPhoto);
        }

        tvName.setText(photo.name);

        return convertView;
    }
}
