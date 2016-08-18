package com.udacity.popmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Xiaoying on 8/3/16.
 */
public class MovieContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.udacity.popmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.udacity.popmovies/movie/ is a valid path for
    // looking at movie data. content://com.udacity.popmovies/givemeroot/ will fail,
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_MOST_POPULAR = "most_popular";
    public static final String PATH_HIGHEST_RATED = "highest_rated";
    public static final String PATH_FAVORITES = "favorites";

    // Column with the foreign key into the movies table.
    public static final String COLUMN_MOVIE_ID_KEY = "movie_id";

    /* Inner class that defines the table contents of the movies table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";

        private static final String[] COLUMNS = {_ID, COLUMN_ORIGINAL_TITLE, COLUMN_POSTER_PATH,
                COLUMN_OVERVIEW, COLUMN_RELEASE_DATE, COLUMN_VOTE_AVERAGE, COLUMN_BACKDROP_PATH};

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static String[] getColumns() {
            return COLUMNS.clone();
        }
    }

    /* Inner class that defines the table contents of the movies table */
    public static final class MostPopularMoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                MovieEntry.CONTENT_URI.buildUpon().appendPath(PATH_MOST_POPULAR).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES
                        + "/" + PATH_MOST_POPULAR;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES
                        + "/" + PATH_MOST_POPULAR;

        public static final String TABLE_NAME = "most_popular_movies";

        private static final String[] COLUMNS = {_ID, COLUMN_MOVIE_ID_KEY};

        public static Uri buildMostPopularMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String[] getColumns() {
            return COLUMNS.clone();
        }
    }

    /* Inner class that defines the table contents of the movies table */
    public static final class HighestRatedMoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                MovieEntry.CONTENT_URI.buildUpon().appendPath(PATH_HIGHEST_RATED).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES
                        + "/" + PATH_HIGHEST_RATED;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES
                        + "/" + PATH_HIGHEST_RATED;

        public static final String TABLE_NAME = "highest_rated_movies";

        private static final String[] COLUMNS = {_ID, COLUMN_MOVIE_ID_KEY};

        public static Uri buildHighestRatedMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String[] getColumns() {
            return COLUMNS.clone();
        }
    }

    /* Inner class that defines the table contents of the favorites table */
    public static final class FavoriteEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                MovieEntry.CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES
                        + "/" + PATH_FAVORITES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES
                        + "/" + PATH_FAVORITES;

        public static final String TABLE_NAME = "favorites";

        private static final String[] COLUMNS = {_ID, COLUMN_MOVIE_ID_KEY};

        public static Uri buildFavoriteMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String[] getColumns() {
            return COLUMNS.clone();
        }
    }
}
