package com.udacity.popmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.udacity.popmovies.data.MovieContract;

/**
 * Created by Administrator on 2016/6/4.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

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
    private int mPosition;

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        if(itemId == R.id.popular){
            sharedPrefs.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_popular_key)).commit();
            updateMovie();
            Loader loader = new Loader(getContext());
            loader.startLoading();
            return true;
        }
        if(itemId == R.id.rated){
            sharedPrefs.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_rated_key)).commit();
            updateMovie();
            Loader loader = new Loader(getContext());
            loader.startLoading();
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
        String sort = Utility.getPreferredSort(getActivity());
        fetchMovieTask.execute(sort);
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
        int count = cursor.getCount();
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
}