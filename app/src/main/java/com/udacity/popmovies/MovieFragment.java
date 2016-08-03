package com.udacity.popmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/6/4.
 */
public class MovieFragment extends Fragment {

    public static final String POSTER_PATH = "poster_path";
    public static final String ORIGINAL_TITLE = "original_title";
    public static final String RELEASE_DATE = "release_date";
    public static final String VOTE_AVERAGE = "vote_date";
    public static final String OVERVIEW = "overview";

    private MovieAdapter mMovieAdapter;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        if(itemId == R.id.popular){
            sharedPrefs.edit().putString(getString(R.string.order), getString(R.string.pref_popular_key)).commit();
            updateMovie();
            return true;
        }
        if(itemId == R.id.rated){
            sharedPrefs.edit().putString(getString(R.string.order), getString(R.string.pref_rated_key)).commit();
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    private void updateMovie() {
        FetchMovieTask fetchMovieTask = new FetchMovieTask(getContext());
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String order = sharedPrefs.getString(getString(R.string.order), getString(R.string.pref_popular_key));
        fetchMovieTask.execute(order);
        mMovieAdapter.swapMovie(FetchMovieTask.mMovies);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_movie);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // The MovieAdapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        mMovieAdapter = new MovieAdapter(getActivity(), new MovieAdapter.MovieAdapterOnClickHandler(){

            @Override
            public void onClick(int position, MovieAdapter.MovieAdapterViewHolder vh) {
                ArrayList<Movie> movies = mMovieAdapter.getMovies();
                Movie movie = movies.get(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(POSTER_PATH, movie.getPoster_path())
                        .putExtra(ORIGINAL_TITLE, movie.getOriginal_title())
                        .putExtra(RELEASE_DATE, movie.getRelease_date())
                        .putExtra(VOTE_AVERAGE, movie.getVote_average())
                        .putExtra(OVERVIEW, movie.getOverview());
                startActivity(intent);
            }
        });

        mRecyclerView.setAdapter(mMovieAdapter);

        return rootView;
    }
}