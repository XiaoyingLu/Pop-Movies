package com.udacity.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.udacity.popmovies.data.MovieContract;
import com.udacity.popmovies.data.api.DiscoverResponse;
import com.udacity.popmovies.data.api.MovieApi;
import com.udacity.popmovies.data.api.MovieApiService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Xiaoying on 8/11/16.
 */
public class MovieNetHelper {

    private static volatile MovieApi movieApi;

    private final Context mContext;
    private volatile boolean loading = false;
    private final Uri mSortedUri;
    private static final String LOG_TAG = MovieNetHelper.class.getSimpleName();
    public static final String BROADCAST_UPDATE_FINISHED = "UpdateFinished";
    public static final String EXTRA_IS_SUCCESSFUL_UPDATED = "isSuccessfulUpdated";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MOVIES_STATUS_OK, MOVIES_STATUS_SERVER_DOWN, MOVIES_STATUS_SERVER_INVALID,
            MOVIES_STATUS_UNKNOWN})
    public @interface MoviesStatus {
    }

    public static final int MOVIES_STATUS_OK = 0;
    public static final int MOVIES_STATUS_SERVER_DOWN = 1;
    public static final int MOVIES_STATUS_SERVER_INVALID = 2;
    public static final int MOVIES_STATUS_UNKNOWN = 3;
//    public static final int MOVIES_STATUS_INVALID = 4;


    public MovieNetHelper(Context context) {
        mContext = context.getApplicationContext();
        mSortedUri = Utility.getSortedUri(context);
    }

    public void updateMovies(String sort){
        callDiscoverMovies(sort, null);
    }

    private void callDiscoverMovies(String sort, @Nullable Integer page) {
        MovieApi service = MovieApiService.getMovieApi(mContext);

        Uri uri = Utility.getSortedUri(mContext);
        if (null == uri) {
            Log.e(LOG_TAG, "Wrong uri: " +uri);
            return;
        }
        Log.e(LOG_TAG, uri.toString());

        service.discoverMovies(sort, page)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<DiscoverResponse<Movie>, List<Movie>>() {
                    @Override
                    public List call(DiscoverResponse<Movie> movieDiscoverResponse) {
                        int page = movieDiscoverResponse.getPage();
                        if (page == 1) {
                            mContext.getContentResolver().delete(
                                    uri,
                                    null,
                                    null
                            );
                        }
                        Log.e(LOG_TAG, "page == " + page + ", " + movieDiscoverResponse.getResults().toString());
                        return movieDiscoverResponse.getResults();
                    }
                })
                .map(new Func1<List<Movie>, Boolean>() {
                    @Override
                    public Boolean call(List<Movie> movies) {
                        if (movies.size() == 0) {
                            // Stream was empty.  No point in parsing.
                            setMoviesStatus(mContext, MOVIES_STATUS_SERVER_DOWN);
                            return null;
                        }
                        for (int i = 0; i < movies.size(); i++) {
                            Uri movieUri = mContext.getContentResolver().insert(
                                    MovieContract.MovieEntry.CONTENT_URI,
                                    movies.get(i).toContentValues());

                            long movie_id = MovieContract.MovieEntry.getIdFromUri(movieUri);
                            Log.e(LOG_TAG, String.valueOf(movie_id));
                            ContentValues entryValues = new ContentValues();
                            entryValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movie_id);
                            mContext.getContentResolver().insert(uri, entryValues);
                            Log.e(LOG_TAG, uri.toString());
                        }
                        Log.e(LOG_TAG, movies.toString());
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        loading = false;
                        setMoviesStatus(mContext, MOVIES_STATUS_OK);
                        sendUpdateFinishedBroadcast(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getLocalizedMessage());
                        setMoviesStatus(mContext, MOVIES_STATUS_SERVER_INVALID);
                        loading = false;
                        sendUpdateFinishedBroadcast(false);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        // do nothing
                    }
                });
//        service.discoverMovies(sort, page)
//                .subscribeOn(Schedulers.newThread())
//                .doOnNext(discoverMoviesResponse -> clearMoviesSortTableIfNeeded(discoverMoviesResponse))
//                .doOnNext(discoverMoviesResponse -> logResponse(discoverMoviesResponse))
//                .map(discoverMoviesResponse -> discoverMoviesResponse.getResults())
//                .flatMap(movies -> Observable.from(movies))
//                .map(movie -> saveMovie(movie))
//                .map(movieUri -> MovieContract.MovieEntry.getIdFromUri(movieUri))
//                .doOnNext(movieId -> saveMovieReference(movieId))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Long>() {
//                    @Override
//                    public void onCompleted() {
//                        loading = false;
//                        sendUpdateFinishedBroadcast(true);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        loading = false;
//                        sendUpdateFinishedBroadcast(false);
//                    }
//
//                    @Override
//                    public void onNext(Long aLong) {
//                        // do nothing
//                    }
//                });
    }

    private void clearMoviesSortTableIfNeeded(DiscoverResponse<Movie> discoverMoviesResponse) {

        if (discoverMoviesResponse.getPage() == 1) {
            mContext.getContentResolver().delete(
                    mSortedUri,
                    null,
                    null
            );
        }
    }

    private Uri saveMovie(Movie movie) {
        return mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, movie.toContentValues());
    }

    private void saveMovieReference(Long movieId) {
        ContentValues entry = new ContentValues();
        entry.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieId);
        mContext.getContentResolver().insert(mSortedUri, entry);
    }

    private void logResponse(DiscoverResponse<Movie> discoverMoviesResponse) {
        Log.d(LOG_TAG, "page == " + discoverMoviesResponse.getPage() + " " +
                discoverMoviesResponse.getResults().toString());
    }

    private void sendUpdateFinishedBroadcast(boolean successfulUpdated) {
        Intent intent = new Intent(BROADCAST_UPDATE_FINISHED);
        intent.putExtra(EXTRA_IS_SUCCESSFUL_UPDATED, successfulUpdated);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        Log.e(LOG_TAG, "sendUpdateFinishedBroadcast-------------" + successfulUpdated);
    }

    /**
     * Sets the movies status into shared preference. This function should not be called from
     * the UI thread because it uses commit to write to the shared preferences.
     *
     * @param c              Context to get the PreferenceManager from.
     * @param moviesStatus The IntDef value to set
     */
    static private void setMoviesStatus(Context c, @MoviesStatus int moviesStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_movies_status_key), moviesStatus);
        spe.commit();
    }

//    public void callMovieVideos(long id, MovieVideosAdapter movieVideosAdapter) {
//        MovieApi service = MovieApiService.getMovieApi(mContext);
//        service.getMovieVideos(id)
//                .subscribeOn(Schedulers.newThread())
//                .map(MovieVideosResponse::getResults)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<ArrayList<MovieVideo>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(LOG_TAG, e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(ArrayList<MovieVideo> movieVideos) {
//                        movieVideosAdapter.setMovieVideos(movieVideos);
//                    }
//                });
//    }
//
//    public void callMovieReviews(long id, MovieReviewsAdapter movieReviewsAdapter) {
//        MovieApi service = MovieApiService.getMovieApi(mContext);
//        service.getMovieReviews(id)
//                .subscribeOn(Schedulers.newThread())
//                .map(MovieReviewsResponse::getResults)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<ArrayList<MovieReview>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(LOG_TAG, e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(ArrayList<MovieReview> movieReviews) {
//                        movieReviewsAdapter.setMovieReviews(movieReviews);
//                    }
//                });
//    }
}
