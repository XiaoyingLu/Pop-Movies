package com.udacity.popmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.popmovies.data.MovieContract;

/**
 * Created by Administrator on 2016/6/4.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String POSTER_PATH = "poster_path";
    public static final String ORIGINAL_TITLE = "original_title";
    public static final String RELEASE_DATE = "release_date";
    public static final String VOTE_AVERAGE = "vote_date";
    public static final String OVERVIEW = "overview";

    private MovieAdapter mMovieAdapter;
    private RecyclerView mRecyclerView;

    private static final int MOVIE_LOADER = 0;
    private static final String COLUMN_SORTED_ID = "sorted_id";
    private static final String[] MOVIE_COLUMNS = {
            COLUMN_SORTED_ID,
            MovieContract.COLUMN_MOVIE_ID_KEY,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
    };

    // These indices are tied to MOVIE_COLUMNS. If MOVIE_COLUMNS changes, these must change.
    static final int COL_SORTED_ID = 0;
    static final int COL_MOVIE_ID_KEY = 1;
    static final int COL_MOVIE_ID = 2;
    static final int COL_MOVIE_ORIGINAL_TITLE = 3;
    static final int COL_MOVIE_POSTER_PATH = 4;
    static final int COL_MOVIE_OVERVIEW = 5;
    static final int COL_MOVIE_RELEASE_DATE = 6;
    static final int COL_MOVIE_VOTE_AVERAGE = 7;

    private MovieNetHelper mMovieNetHelper;
    private int mPosition;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MovieNetHelper.BROADCAST_UPDATE_FINISHED)) {
                if (!intent.getBooleanExtra(MovieNetHelper.EXTRA_IS_SUCCESSFUL_UPDATED, true)) {
                    Toast.makeText(getContext(), R.string.error_failed_to_update_movies, Toast.LENGTH_SHORT)
                            .show();
//                    Snackbar.make(swipeRefreshLayout, R.string.error_failed_to_update_movies,
//                            Snackbar.LENGTH_LONG)
//                            .show();
                }
//                swipeRefreshLayout.setRefreshing(false);
//                endlessRecyclerViewOnScrollListener.setLoading(false);
//                updateGridLayout();
            }
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ( key.equals(R.string.pref_movies_status_key)){
            updateEmptyView();
        }
    }


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dataUri, MovieAdapter.MovieAdapterViewHolder vh);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMovieNetHelper = new MovieNetHelper(getContext());
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



    private void updateMovie() {
//        FetchMovieTask fetchMovieTask = new FetchMovieTask(getContext());
        String sort = Utility.getPreferredSort(getActivity());
//        fetchMovieTask.execute(sort);
        mMovieNetHelper.updateMovies(sort);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        mMovieNetHelper.updateMovies(Utility.getPreferredSort(getContext()));

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
            public void onClick(long id, MovieAdapter.MovieAdapterViewHolder vh) {

                ((Callback)getActivity())
                        .onItemSelected(MovieContract.MovieEntry.buildMovieUri(id), vh);
                mPosition = vh.getAdapterPosition();
            }
        });

        mRecyclerView.setAdapter(mMovieAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        Uri uri = Utility.getSortedUri(getContext());
        if (null == uri) {
            throw new IllegalStateException("Unknown sort.");
        }

        return new CursorLoader(getActivity(),
                uri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMovieAdapter.swapCursor(cursor);
//        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

    public void onSortChanged() {
        updateMovie();
        Loader loader = new Loader(getContext());
        loader.startLoading();
    }

    private void updateEmptyView(){
        if (mMovieAdapter.getItemCount() == 0){
            TextView tv = (TextView) getView().findViewById(R.id.recyclerview_movie_empty);
            if (null != tv){
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.empty_movie_list;
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
                tv.setText(message);
            }
        }
    }
}