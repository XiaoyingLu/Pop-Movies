package com.udacity.popmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Administrator on 2016/6/4.
 */
public class DetailFragment extends Fragment {
    final String PICTURE_BASE_URL = "http://image.tmdb.org/t/p/";
    final String PICTURE_SIZE = "w185";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        TextView movieTitle = (TextView) rootView.findViewById(R.id.textview_title);
        ImageView moviePoster = (ImageView) rootView.findViewById(R.id.imageview_poster);
        TextView movieDate = (TextView) rootView.findViewById(R.id.textview_date);
        TextView movieVoteAverage = (TextView) rootView.findViewById(R.id.textview_vote_average);
        TextView movieOverview = (TextView) rootView.findViewById(R.id.textview_overview);

        Intent intent = getActivity().getIntent();
        movieTitle.setText(intent.getStringExtra(MovieFragment.ORIGINAL_TITLE));
        Picasso.with(getContext()).
                load(PICTURE_BASE_URL + PICTURE_SIZE + "//" + intent.getStringExtra(MovieFragment.POSTER_PATH))
                .into(moviePoster);
        movieDate.setText(intent.getStringExtra(MovieFragment.RELEASE_DATE));
        movieVoteAverage.setText(intent.getStringExtra(MovieFragment.VOTE_AVERAGE));
        movieOverview.setText(intent.getStringExtra(MovieFragment.OVERVIEW));

        return rootView;
    }
}
