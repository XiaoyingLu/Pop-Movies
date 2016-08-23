package com.udacity.popmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.udacity.popmovies.data.MovieContract.FavoriteEntry;
import com.udacity.popmovies.data.MovieContract.MovieEntry;
import com.udacity.popmovies.data.MovieContract.MostPopularMoviesEntry;
import com.udacity.popmovies.data.MovieContract.HighestRatedMoviesEntry;

/**
 * Created by Xiaoying on 8/3/16.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL," +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_BACKDROP_PATH + " TEXT " +
                " );";

        final String SQL_CREATE_MOST_POPULAR_MOVIES_TABLE = "CREATE TABLE " + MostPopularMoviesEntry.TABLE_NAME + " (" +
                MostPopularMoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.COLUMN_MOVIE_ID_KEY + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + MovieContract.COLUMN_MOVIE_ID_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") " +
                " );";

        final String SQL_CREATE_HIGHEST_RATED_MOVIES_TABLE = "CREATE TABLE " + HighestRatedMoviesEntry.TABLE_NAME + " (" +
                HighestRatedMoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.COLUMN_MOVIE_ID_KEY + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + MovieContract.COLUMN_MOVIE_ID_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") " +
                " );";

        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.COLUMN_MOVIE_ID_KEY + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + MovieContract.COLUMN_MOVIE_ID_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") " +
                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOST_POPULAR_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_HIGHEST_RATED_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MostPopularMoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HighestRatedMoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
