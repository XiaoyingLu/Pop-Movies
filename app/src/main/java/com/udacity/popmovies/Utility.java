package com.udacity.popmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

    static public boolean isNetworkAvailable(Context c){
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     *
     * @param c
     * @return
     */
    @SuppressWarnings("ResourceType")
    static public @MovieNetHelper.MoviesStatus
    int getMoviesStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_movies_status_key),
                MovieNetHelper.MOVIES_STATUS_UNKNOWN);
    }

    /**
     * Resets the location status. (Sets it to MovieNetHelper.MOVIES_STATUS_UNKNOWN)
     * @param c Context used to get the SharedPreferences
     */
    static public void resetMoviesStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_movies_status_key),
                MovieNetHelper.MOVIES_STATUS_UNKNOWN);
        spe.apply(); // not commit() cause this  method will be used in the UI thread
    }

    public static long getTotalMoviesNum(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(context.getString(R.string.pref_movies_total_results), 0);
    }
}
