package com.udacity.popmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.udacity.popmovies.data.MovieContract;

public class Utility {

    public static String getPreferredSort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_default));
    }

    public static Uri getSortedUri(Context context) {
        String sort = getPreferredSort(context);
        if (sort.equals(context.getString(R.string.pref_popular_key))) {
            return MovieContract.MostPopularMoviesEntry.CONTENT_URI;
        } else if (sort.equals(context.getString(R.string.pref_rated_key))) {
            return MovieContract.HighestRatedMoviesEntry.CONTENT_URI;
        } else {
            return null;
        }
    }

    public static String getSortedTableId(Context context) {
        String sort = getPreferredSort(context);
        if (sort.equals(context.getString(R.string.pref_popular_key))) {
            return MovieContract.MostPopularMoviesEntry.TABLE_NAME + MovieContract.MostPopularMoviesEntry._ID;
        } else if (sort.equals(context.getString(R.string.pref_rated_key))) {
            return MovieContract.HighestRatedMoviesEntry.TABLE_NAME + MovieContract.HighestRatedMoviesEntry._ID;
        } else {
            return null;
        }
    }
}
