package com.udacity.popmovies;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.udacity.popmovies.data.MovieContract;

/**
 * Created by Administrator on 2016/6/4.
 */
public abstract class BaseFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String POSTER_PATH = "poster_path";
    public static final String ORIGINAL_TITLE = "original_title";
    public static final String RELEASE_DATE = "release_date";
    public static final String VOTE_AVERAGE = "vote_date";
    public static final String OVERVIEW = "overview";

    public MovieAdapter mMovieAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    public RecyclerView mRecyclerView;
    public TextView mEmptyView;
    private boolean mHoldForTransition;

    private static final int MOVIE_LOADER = 0;
    public static final int MOVIE_FAVORITE_LOADER = 1;
    public static String FAVORIE_LOADER = "movie_favorite_loader";

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
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,
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
    static final int COL_MOVIE_BACKDROP_PATH = 8;

    private int mPosition;
    private FavoriteService favoriteService;
    public View mRootView;
    public GridLayoutManager mGridLayoutManager;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(long id, Uri dataUri, MovieAdapter.MovieAdapterViewHolder vh);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("MF", "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        favoriteService = new FavoriteService(getActivity());
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.MovieFragment,
                0, 0);
        mHoldForTransition = a.getBoolean(R.styleable.MovieFragment_sharedElementTransitions, false);
        a.recycle();
    }

    public abstract void initSwipeRefreshLayout();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_movie, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        initSwipeRefreshLayout();
//        mMovieNetHelper.updateMovies(Utility.getPreferredSort(getContext()));

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview_movie);
        mEmptyView = (TextView) mRootView.findViewById(R.id.recyclerview_movie_empty);
        mEmptyView.setVisibility(View.GONE);

        // Set the layout manager
        int grid_column_num = getResources().getInteger(R.integer.grid_column_num);
        mGridLayoutManager = new GridLayoutManager(getActivity(), grid_column_num);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // The MovieAdapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        mMovieAdapter = new MovieAdapter(getActivity(), new MovieAdapter.MovieAdapterOnClickHandler() {

            @Override
            public void onFavoriteClick(long id) {
                if (favoriteService.isFavorite(id)) {
                    favoriteService.removeFromFavorite(id);
                    Snackbar.make(mRootView, "Removed from favorite", Snackbar.LENGTH_SHORT).show();
                } else {
                    favoriteService.addToFavorite(id);
                    Snackbar.make(mRootView, "Added to favorite", Snackbar.LENGTH_SHORT).show();
                }
                mMovieAdapter.notifyDataSetChanged();
                updateEmptyView();
            }

            @Override
            public void onImageClick(long id, MovieAdapter.MovieAdapterViewHolder vh) {

                ((Callback) getActivity())
                        .onItemSelected(id, MovieContract.MovieEntry.buildMovieUri(id), vh);
                mPosition = vh.getAdapterPosition();
            }

            @Override
            public void onFooterClick() {
                reloadMovies();
            }
        }, mEmptyView);

        mRecyclerView.setAdapter(mMovieAdapter);

        final AppBarLayout appbarView = (AppBarLayout) mRootView.findViewById(R.id.appbar);
        if (null != appbarView) {
            ViewCompat.setElevation(appbarView, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (0 == mRecyclerView.computeVerticalScrollOffset()) {
                            appbarView.setElevation(0);
                        } else {
                            appbarView.setElevation(appbarView.getTargetElevation());
                        }
                    }
                });
            }
        }

        return mRootView;
    }

    protected abstract void reloadMovies();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // We hold for transition here just in-case the activity
        // needs to be re-created. In a standard return transition,
        // this doesn't actually make a difference.
        if (mHoldForTransition) {
            getActivity().supportPostponeEnterTransition();
        }
        initMovieLoader();
        super.onActivityCreated(savedInstanceState);
    }

    public abstract void initMovieLoader();
    public abstract Uri getMovieUri();

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        Uri uri= getMovieUri();
        if (null == uri){
            throw new NullPointerException("Uri is not initialized");
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
        updateEmptyView();
        mRecyclerView.setVisibility(View.VISIBLE);
        if (cursor.getCount() == 0) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                        if (mHoldForTransition) {
                            getActivity().supportStartPostponedEnterTransition();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
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

    public abstract void updateEmptyView();
}