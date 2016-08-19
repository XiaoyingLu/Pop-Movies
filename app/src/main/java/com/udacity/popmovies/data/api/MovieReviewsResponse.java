package com.udacity.popmovies.data.api;

import com.google.gson.annotations.SerializedName;
import com.udacity.popmovies.MovieReview;

import java.util.ArrayList;


public class MovieReviewsResponse {
    @SerializedName("id")
    private long movieId;

    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private ArrayList<MovieReview> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    public MovieReviewsResponse(long movieId, int page, ArrayList<MovieReview> results,
                                int totalPages, int totalResults) {
        this.movieId = movieId;
        this.page = page;
        this.results = results;
        this.totalPages = totalPages;
        this.totalResults = totalResults;
    }

    public long getMovieId() {
        return movieId;
    }

    public int getPage() {
        return page;
    }

    public ArrayList<MovieReview> getResults() {
        return results;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalResults() { return totalResults; }
}
