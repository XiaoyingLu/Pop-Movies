package com.udacity.popmovies.data.api;


import com.udacity.popmovies.Movie;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Xiaoying on 8/11/16.
 */
public interface MovieApi {

    @GET("movie/{id}")
    Observable<Movie> getMovie(@Path("id") long id);

    //http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&page=1
    @GET("discover/movie")
    Observable<DiscoverResponse<Movie>> discoverMovies(@Query("sort_by") String sortBy, @Query("page") Integer page);

}
