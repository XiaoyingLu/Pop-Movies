package com.udacity.popmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Administrator on 2016/6/4.
 */
public class MovieListAdapter extends ArrayAdapter<Movie> {
    final String PICTURE_BASE_URL = "http://image.tmdb.org/t/p/";
    final String PICTURE_SIZE = "w185";

    public MovieListAdapter(Context context, List<Movie> movieList) {
        super(context, 0, movieList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie, parent, false);

        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_movie_imageview);
        Picasso.with(getContext()).
                load(PICTURE_BASE_URL + PICTURE_SIZE + "//" + movie.getPoster_path())
                .into(imageView);
        return convertView;
    }
}
