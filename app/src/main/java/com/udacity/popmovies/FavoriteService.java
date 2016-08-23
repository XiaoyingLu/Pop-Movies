package com.udacity.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.udacity.popmovies.data.MovieContract;

/**
 * Created by Xiaoying on 8/22/16.
 */
public class FavoriteService {

    private final Context mContext;

    public FavoriteService(Context context) {
        mContext = context;
    }

    public void addToFavorite(long id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, id);
        mContext.getContentResolver().insert(MovieContract.FavoriteEntry.CONTENT_URI, contentValues);
    }

    public void removeFromFavorite(long id) {
        mContext.getContentResolver().delete(MovieContract.FavoriteEntry.CONTENT_URI,
                MovieContract.COLUMN_MOVIE_ID_KEY + "=" + id, null);
    }

    public boolean isFavorite(long id) {
        Cursor cursor = mContext.getContentResolver().query(MovieContract.FavoriteEntry.CONTENT_URI,
                new String[]{MovieContract.COLUMN_MOVIE_ID_KEY},
                MovieContract.COLUMN_MOVIE_ID_KEY + "=?",
                new String[]{String.valueOf(id)},
                null,
                null);
        if (cursor != null && cursor.getCount() > 0){
            cursor.close();
            return true;
        }
        return false;
    }
}
