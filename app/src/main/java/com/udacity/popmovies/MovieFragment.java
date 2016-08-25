package com.udacity.popmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;

/**
 * Created by Administrator on 2016/6/4.
 */
public class MovieFragment extends BaseFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener {

    private static final int MOVIE_LOADER = 0;
    public static final int MOVIE_FAVORITE_LOADER = 1;

    private static final String COLUMN_SORTED_ID = "sorted_id";

    private MovieNetHelper mMovieNetHelper;
    private int mPosition;
    private FavoriteService favoriteService;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MovieNetHelper.BROADCAST_UPDATE_FINISHED)) {
                if (!intent.getBooleanExtra(MovieNetHelper.EXTRA_IS_SUCCESSFUL_UPDATED, true)) {
                    mMovieAdapter.setFooterVisibility(true);
                    mMovieAdapter.updateFooterView(mMovieAdapter.LOAD_ERROR);
//                    Snackbar.make(mSwipeRefreshLayout, R.string.error_failed_to_update_movies,
//                            Snackbar.LENGTH_LONG)
//                            .show();
                }
                mSwipeRefreshLayout.setRefreshing(false);
                mMovieAdapter.updateFooterView(MovieAdapter.PULL_TO_LOAD_MORE);
                getLoaderManager().restartLoader(MOVIE_LOADER, null, MovieFragment.this);
            }
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(R.string.pref_movies_status_key)) {
            updateEmptyView();
        }
    }

    @Override
    public void onRefresh() {
        Log.e("MF", "onRefresh");
        mSwipeRefreshLayout.setRefreshing(true);
        mMovieNetHelper.updateMovies(Utility.getPreferredSort(getContext()));

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("MF", "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMovieNetHelper = new MovieNetHelper(getContext());
        favoriteService = new FavoriteService(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MovieNetHelper.BROADCAST_UPDATE_FINISHED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
//        if (endlessRecyclerViewOnScrollListener != null) {
//            endlessRecyclerViewOnScrollListener.setLoading(moviesService.isLoading());
//        }
//        swipeRefreshLayout.setRefreshing(moviesService.isLoading());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setProgressViewOffset(true, 30, 200);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light, R.color.primary_dark);
    }

    @Override
    protected void reloadMovies() {
        mMovieAdapter.setFooterVisibility(true);
        mMovieAdapter.updateFooterView(MovieAdapter.IS_LOADING);
        mMovieNetHelper.loadMoreMovies();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.addOnScrollListener(new LoadScrollListener() {
            @Override
            public void onLoad(int totalItemCount) {
                mMovieAdapter.setFooterVisibility(true);
                if (totalItemCount == Utility.getTotalMoviesNum(getActivity())) {
                    mMovieAdapter.updateFooterView(MovieAdapter.NO_MORE_LOAD);
                    return;
                }
                mMovieAdapter.updateFooterView(MovieAdapter.IS_LOADING);
                mMovieNetHelper.loadMoreMovies();
            }
        });
    }

    @Override
    public void initMovieLoader() {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public Uri getMovieUri() {
        Uri uri = Utility.getSortedUri(getContext());
        return uri;
    }

    public void updateEmptyView() {
        if (mMovieAdapter.getItemCount() == 0) {
            if (null != mEmptyView) {
                int message = R.string.empty_movie_list;

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