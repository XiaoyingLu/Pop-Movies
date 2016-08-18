package com.udacity.popmovies;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.udacity.popmovies.data.MovieContract;

public class Movie implements Parcelable{

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @SerializedName("id")
    long id;
    @SerializedName("poster_path")
    String poster_path;
    @SerializedName("overview")
    String overview;
    @SerializedName("original_title")
    String original_title;
    @SerializedName("release_date")
    String release_date;
    @SerializedName("vote_average")
    String vote_average;
    @SerializedName("backdrop_path")
    private String backdrop_path;

    protected Movie(Parcel in) {
        id = in.readLong();
        poster_path = in.readString();
        overview = in.readString();
        original_title = in.readString();
        release_date = in.readString();
        vote_average = in.readString();
        backdrop_path = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getVote_average() {
        return vote_average;
    }

    public void setVote_average(String vote_average) {
        this.vote_average = vote_average;
    }

    public String getBackdropPath() {
        return backdrop_path;
    }

    public void setBackdropPath(String backdrop_Path) {
        this.backdrop_path = backdrop_Path;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry._ID, id);
        values.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, original_title);
        values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, poster_path);
        values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, release_date);
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
        values.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, backdrop_path);
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(poster_path);
        parcel.writeString(overview);
        parcel.writeString(original_title);
        parcel.writeString(release_date);
        parcel.writeString(vote_average);
        parcel.writeString(backdrop_path);
    }
}
