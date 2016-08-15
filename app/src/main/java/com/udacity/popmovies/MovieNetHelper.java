package com.udacity.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.udacity.popmovies.data.MovieContract;
import com.udacity.popmovies.data.api.DiscoverResponse;
import com.udacity.popmovies.data.api.MovieApi;
import com.udacity.popmovies.data.api.MovieApiService;

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
        Log.d(LOG_TAG, uri.toString());

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
                        Log.d(LOG_TAG, "page == " + page + ", " + movieDiscoverResponse.getResults().toString());
                        return movieDiscoverResponse.getResults();
                    }
                })
                .map(new Func1<List<Movie>, Boolean>() {
                    @Override
                    public Boolean call(List<Movie> movies) {
                        for (int i = 0; i < movies.size(); i++) {
                            Uri movieUri = mContext.getContentResolver().insert(
                                    MovieContract.MovieEntry.CONTENT_URI,
                                    movies.get(i).toContentValues());

                            long movie_id = MovieContract.MovieEntry.getIdFromUri(movieUri);
                            ContentValues entryValues = new ContentValues();
                            entryValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movie_id);
                            mContext.getContentResolver().insert(uri, entryValues);
                        }
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        loading = false;
                        sendUpdateFinishedBroadcast(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getLocalizedMessage());
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
    }
}
