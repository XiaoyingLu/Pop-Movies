package com.udacity.popmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.popmovies.data.MovieContract;
import com.udacity.popmovies.data.api.MovieApi;
import com.udacity.popmovies.data.api.MovieApiService;
import com.udacity.popmovies.data.api.MovieReviewsResponse;
import com.udacity.popmovies.data.api.MovieVideosResponse;

import java.util.ArrayList;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/6/4.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String MOVIE_ID = "movie_id";
    final String PICTURE_BASE_URL = "http://image.tmdb.org/t/p/";
    final String PICTURE_SIZE = "w780";
    private static final int DETAIL_LOADER = 0;
    static final String DETAIL_URI = "URI";
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH
    };

    // These indices are tied to DETAIL_COLUMNS. If DETAIL_COLUMNS changes, these must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_ORIGINAL_TITLE = 1;
    static final int COL_MOVIE_POSTER_PATH = 2;
    static final int COL_MOVIE_OVERVIEW = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_VOTE_AVERAGE = 5;
    static final int COL_MOVIE_BACKDROP_PATH = 6;
    private TextView mMovieTitle;
    private ImageView mMoviePoster;
    private TextView mMovieDate;
    private TextView mMovieVoteAverage;
    private TextView mMovieOverview;
    private boolean mTransitionAnimation;
    private Uri mUri;
    private RatingBar mMovieVoteRate;
    private RecyclerView mMovieVideos;
    private RecyclerView mMovieReviews;
    private MovieNetHelper mMovieNetHelper;
    private CardView mCardMovieVideos;
    private CardView mCardMovieReviews;
    private MovieVideosAdapter mMovieVideosAdapter;
    private MovieReviewsAdapter mMovieReviewsAdapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onTitleLoaded(String original_title, String backdrop_path);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovieNetHelper = new MovieNetHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if(arguments != null){
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mMoviePoster = (ImageView) rootView.findViewById(R.id.imageview_poster);
        mMovieTitle = (TextView) rootView.findViewById(R.id.textview_title);
        mMovieDate = (TextView) rootView.findViewById(R.id.textview_date);
        mMovieVoteRate = (RatingBar) rootView.findViewById(R.id.rating_vote);
        mMovieVoteAverage = (TextView) rootView.findViewById(R.id.textview_vote_average);
        mMovieOverview = (TextView) rootView.findViewById(R.id.textview_overview);
        mCardMovieVideos = (CardView) rootView.findViewById(R.id.movie_videos_container);
        mMovieVideos = (RecyclerView) rootView.findViewById(R.id.recyclerview_movie_videos);
        mCardMovieReviews = (CardView) rootView.findViewById(R.id.movie_reviews_container);
        mMovieReviews = (RecyclerView) rootView.findViewById(R.id.recyclerview_movie_reviews);

        long id = getActivity().getIntent().getLongExtra(MOVIE_ID, 0);
        initMovieVideos(id);
        initMovieReviews(id);

        return rootView;
    }

    private void initMovieVideos(long id) {
        mMovieVideosAdapter = new MovieVideosAdapter(getActivity(),
                new MovieVideosAdapter.MovieVideosAdapterOnClickHandler() {
                    @Override
                    public void onClick(MovieVideo movieVideo) {
                        if (movieVideo != null && movieVideo.isYoutubeVideo()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://www.youtube.com/watch?v=" + movieVideo.getKey()));
                            startActivity(intent);
                        }
                    }
                });
        callMovieVideos(id);
//        mMovieNetHelper.callMovieVideos(id, mMovieVideosAdapter);
        mMovieVideos.setAdapter(mMovieVideosAdapter);
        mMovieVideos.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        mMovieVideos.addItemDecoration(new ItemOffsetDecoration(getActivity(), R.dimen.movie_video_item_offset));
                mMovieVideos.setHasFixedSize(true);
    }

    private void callMovieVideos(long id) {
        MovieApi service = MovieApiService.getMovieApi(getActivity());
        service.getMovieVideos(id)
                .subscribeOn(Schedulers.newThread())
                .map(MovieVideosResponse::getResults)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ArrayList<MovieVideo>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(ArrayList<MovieVideo> movieVideos) {
                        mMovieVideosAdapter.setMovieVideos(movieVideos);
                        updateCardMovieVideos();
                    }
                });
    }

    private void updateCardMovieVideos() {
        if (mMovieVideosAdapter == null || mMovieVideosAdapter.getItemCount() == 0) {
            mCardMovieVideos.setVisibility(View.GONE);
        } else {
            mCardMovieVideos.setVisibility(View.VISIBLE);
        }
    }

    private void initMovieReviews(long id) {
        mMovieReviewsAdapter = new MovieReviewsAdapter(getActivity(),
                new MovieReviewsAdapter.MovieReviewsAdapterOnClickHandler() {
                    @Override
                    public void onClick(MovieReview movieReview) {
                        if (movieReview != null && movieReview.getUrl() != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(movieReview.getUrl()));
                            startActivity(intent);
                        }
                    }
                });
//        mMovieNetHelper.callMovieReviews(id, mMovieReviewsAdapter);
        callMovieReviews(id);
        mMovieReviews.setAdapter(mMovieReviewsAdapter);
        mMovieReviews.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));
        mMovieReviews.setHasFixedSize(true);
    }

    private void callMovieReviews(long id) {
        MovieApi service = MovieApiService.getMovieApi(getActivity());
        service.getMovieReviews(id)
                .subscribeOn(Schedulers.newThread())
                .map(MovieReviewsResponse::getResults)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ArrayList<MovieReview>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(ArrayList<MovieReview> movieReviews) {
                        mMovieReviewsAdapter.setMovieReviews(movieReviews);
                        updateCardMovieReviews();
                    }
                });
    }

    private void updateCardMovieReviews() {
        if (mMovieReviewsAdapter == null || mMovieReviewsAdapter.getItemCount() == 0) {
            mCardMovieReviews.setVisibility(View.GONE);
        } else {
            mCardMovieReviews.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");

        if (null != mUri) {
            return new CursorLoader(getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");

        if( data != null && data.moveToFirst()) {

            String poster_path = data.getString(COL_MOVIE_POSTER_PATH);
            Picasso.with(getContext())
                    .load(PICTURE_BASE_URL + PICTURE_SIZE + "//" + poster_path)
                    .error(R.mipmap.ic_launcher)
                    .into(mMoviePoster);

            String original_title = data.getString(COL_MOVIE_ORIGINAL_TITLE);
            mMovieTitle.setText(original_title);
            String overview = data.getString(COL_MOVIE_OVERVIEW);
            mMovieOverview.setText(overview);
            String release_date = data.getString(COL_MOVIE_RELEASE_DATE);
            mMovieDate.setText("Release date: " + release_date);
            String vote_average = data.getString(COL_MOVIE_VOTE_AVERAGE);
            mMovieVoteAverage.setText("Average rating: "+ vote_average);
            mMovieVoteRate.setMax(10);
            mMovieVoteRate.setRating(Float.parseFloat(vote_average));
            String backdrop_path = data.getString(COL_MOVIE_BACKDROP_PATH);

            ((Callback)getActivity()).onTitleLoaded(original_title, backdrop_path);
        }

        if ( mTransitionAnimation ) {
            getActivity().supportStartPostponedEnterTransition();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
