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
 * Created by Felix Huang on 7/13/2015.
 */
public class TrackAdapter extends ArrayAdapter<TrackC> {
    public TrackAdapter(Context context, List<TrackC> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    class ViewHolder {
        public ImageView ivAlbum;
        public TextView tvAlbum;
        public TextView tvTrack;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get item data
        TrackC track = getItem(position);

        ViewHolder holder;
        // check if we're using a recycled view, if not we need to inflate
        if (convertView == null) { // need to create a new view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
            // parent : current container; false : not to attach this item view to container yet
            holder = new ViewHolder();
            holder.ivAlbum = (ImageView) convertView.findViewById(R.id.ivAlbum);
            holder.tvAlbum = (TextView) convertView.findViewById(R.id.tvAlbum);
            holder.tvTrack = (TextView) convertView.findViewById(R.id.tvTrack);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // look up views for populating the data (image, caption)
        ImageView ivAlbum = holder.ivAlbum;
        TextView tvAlbum = holder.tvAlbum;
        TextView tvTrack = holder.tvTrack;

        // insert the model data into each item views
        // clear image view
        ivAlbum.setImageResource(0);

        // insert the image using picasso; send asynch out which runs in background
        if (track.album.imageUrl != null) { // to do : to check more for valid url
            Picasso.with(getContext()).load(track.album.imageUrl).into(ivAlbum);
        }

        tvAlbum.setText(track.album.name);
        tvTrack.setText(track.name);

        return convertView;
    }
}
