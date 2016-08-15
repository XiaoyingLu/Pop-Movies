package com.udacity.popmovies.data.api;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieApiService {

    private static final String BASE_URL = "http://api.themoviedb.org/3/";
    private static final String CACHE_DIR = "HttpResponseCache";
    private static final long CACHE_SIZE = 10 * 1024 * 1024;    // 10 MB
    private static final int CONNECT_TIMEOUT = 15;
    private static final int WRITE_TIMEOUT = 60;
    private static final int TIMEOUT = 60;
    private static MovieApi movieApi;

    public static MovieApi getMovieApi(Context context) {
        synchronized (MovieApi.class) {
            if (movieApi == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .client(getClient(context))
                        .build();
                movieApi = retrofit.create(MovieApi.class);
            }
        }
        return movieApi;
    }

    private static OkHttpClient getClient(Context context) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new AuthorizationInterceptor());

        final File baseDir = context.getCacheDir();
        if (baseDir != null) {
            final File cacheDir = new File(baseDir, CACHE_DIR);
            builder.cache(new Cache(cacheDir, CACHE_SIZE));
        }

        return builder.build();
    }
}
