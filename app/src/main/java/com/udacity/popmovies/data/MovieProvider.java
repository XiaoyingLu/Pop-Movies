package com.udacity.popmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Xiaoying on 8/3/16.
 */
public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int MOVIE_ID = 101;
    static final int MOVIE_MOST_POPULAR = 102;
    static final int MOVIE_HIGHEST_RATED = 103;
    static final int MOVIE_FAVORITE = 104;


    private static final String movieSelection = MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry._ID + " = ? ";

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {

        long id = MovieContract.MovieEntry.getIdFromUri(uri);
        String[] selectionArgs = new String[]{Long.toString(id)};
        String selection = movieSelection;

        return mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMoviesFromReferenceTable(String tableName, String[] projection, String selection,
                                               String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder movieQueryBuilder = new SQLiteQueryBuilder();
        //This is an inner join which looks like
        //favorites INNER JOIN movies ON favorites.movie_id = movies._id
        movieQueryBuilder.setTables(
                tableName + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + tableName + "." + MovieContract.COLUMN_MOVIE_ID_KEY +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID);
        return movieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // MovieContract to help define the types to the UriMatcher.
        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/"
                + MovieContract.PATH_MOST_POPULAR, MOVIE_MOST_POPULAR);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/"
                + MovieContract.PATH_HIGHEST_RATED, MOVIE_HIGHEST_RATED);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/"
                + MovieContract.PATH_FAVORITES, MOVIE_FAVORITE);

        // 3) Return the new matcher!
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_MOST_POPULAR:
                return MovieContract.MostPopularMoviesEntry.CONTENT_TYPE;
            case MOVIE_HIGHEST_RATED:
                return MovieContract.HighestRatedMoviesEntry.CONTENT_TYPE;
            case MOVIE_FAVORITE:
                return MovieContract.FavoriteEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movies"
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "movies/*"
            case MOVIE_ID: {
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }
            // "movies/most_popular"
            case MOVIE_MOST_POPULAR: {
                retCursor = getMoviesFromReferenceTable(
                        MovieContract.MostPopularMoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder);
                break;
            }
            // "movies/highest_rated"
            case MOVIE_HIGHEST_RATED: {
                retCursor = getMoviesFromReferenceTable(
                        MovieContract.HighestRatedMoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder);
                break;
            }
            // "movies/favorites"
            case MOVIE_FAVORITE: {
                retCursor = getMoviesFromReferenceTable(
                        MovieContract.FavoriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insertWithOnConflict(MovieContract.MovieEntry.TABLE_NAME, null,
                        contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 )
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIE_MOST_POPULAR: {
                long _id = db.insert(MovieContract.MostPopularMoviesEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 )
                    returnUri = MovieContract.MostPopularMoviesEntry.buildMostPopularMoviesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIE_HIGHEST_RATED: {
                long _id = db.insert(MovieContract.HighestRatedMoviesEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 )
                    returnUri = MovieContract.HighestRatedMoviesEntry.buildHighestRatedMoviesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIE_FAVORITE: {
                long _id = db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 )
                    returnUri = MovieContract.FavoriteEntry.buildFavoriteMoviesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection="1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ID:
                long id = MovieContract.MovieEntry.getIdFromUri(uri);
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, movieSelection,
                        new String[]{Long.toString(id)});
                break;
            case MOVIE_MOST_POPULAR:
                rowsDeleted = db.delete(MovieContract.MostPopularMoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_HIGHEST_RATED:
                rowsDeleted = db.delete(MovieContract.HighestRatedMoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_FAVORITE:
                rowsDeleted = db.delete(MovieContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the uri listeners if the rowsDeleted != 0 or the selection is null.
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        // return the actual rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        if (null == selection) selection="1";
        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MOVIE_ID:
                long id = MovieContract.MovieEntry.getIdFromUri(uri);
                selectionArgs = new String[] {Long.toString(id)};
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, movieSelection, selectionArgs);
                break;
            case MOVIE_MOST_POPULAR:
                rowsUpdated = db.update(MovieContract.MostPopularMoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MOVIE_HIGHEST_RATED:
                rowsUpdated = db.update(MovieContract.HighestRatedMoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MOVIE_FAVORITE:
                rowsUpdated = db.update(MovieContract.FavoriteEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
