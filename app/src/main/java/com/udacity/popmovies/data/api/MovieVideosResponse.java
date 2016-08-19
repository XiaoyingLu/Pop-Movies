package com.udacity.popmovies.data.api;

import com.google.gson.annotations.SerializedName;
import com.udacity.popmovies.MovieVideo;

import java.util.ArrayList;

public class MovieVideosResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("results")
    private ArrayList<MovieVideo> results;

    public MovieVideosResponse(long id, ArrayList<MovieVideo> results) {
        this.id = id;
        this.results = results;
    }

    public long getId() { return id; }
    public ArrayList<MovieVideo> getResults() {
        return results;
    }

}
