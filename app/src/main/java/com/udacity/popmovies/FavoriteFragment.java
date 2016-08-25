package com.udacity.popmovies;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.udacity.popmovies.data.MovieContract;

/**
 * Created by Administrator on 2016/6/4.
 */
public class FavoriteFragment extends BaseFragment {

    private static final int MOVIE_LOADER = 0;
    public static final int MOVIE_FAVORITE_LOADER = 1;

    private static final String COLUMN_SORTED_ID = "sorted_id";

    private int mPosition;
    private FavoriteService favoriteService;
    private boolean isFavoriteFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("MF", "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setEnabled(false);
    }

    @Override
    protected void reloadMovies() {
        return;
    }

    @Override
    public void initMovieLoader() {
        getLoaderManager().initLoader(MOVIE_FAVORITE_LOADER, null, this);
    }

    @Override
    public Uri getMovieUri() {
        return MovieContract.FavoriteEntry.CONTENT_URI;
    }

    public void updateEmptyView() {
        if (mMovieAdapter.getItemCount() == 1) {
            if (null != mEmptyView) {
                int message = R.string.empty_movie_favorite_list;

                // if cursor is empty, why? do we have an invalid movie
                @MovieNetHelper.MoviesStatus int movies_status = Utility.getMoviesStatus(getActivity());
                switch (movies_status) {
                    case MovieNetHelper.MOVIES_STATUS_SERVER_DOWN:
                        message = R.string.empty_movie_list_server_down;
                        break;
                    case MovieNetHelper.MOVIES_STATUS_SERVER_INVALID:
                        message = R.string.empty_movie_list_server_error;
                        break;
                    default:
                        if (!Utility.isNetworkAvailable(getActivity())) {
                            message = R.string.empty_movie_list_no_network;
                        }
                }
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(getString(message));
            }
        }
    }
}