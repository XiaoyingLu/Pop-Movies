package com.udacity.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.udacity.popmovies.data.MovieContract;
import com.udacity.popmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Xiaoying on 8/2/16.
 */
public class FetchMovieTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final Context mContext;
    private MovieAdapter mMovieAdapter;

    public FetchMovieTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {
            final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/";
            final String APPID = "api_key";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(params[0])
                    .appendQueryParameter(APPID, BuildConfig.OPEN_MOVIE_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Read input stream into a string
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;

            }
            movieJsonStr = buffer.toString();
            getMovieDataFromJson(movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }

    private void getMovieDataFromJson(String movieJsonStr) {

        final String TM_RESULT = "results";
        final String TM_ORIGINAL_TITLE = "original_title";
        final String TM_POSTER = "poster_path";
        final String TM_OVERVIEW = "overview";
        final String TM_DATE = "release_date";
        final String TM_VOTE_AVERAGE = "vote_average";

        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TM_RESULT);

            // Insert the new movie information into the database
//            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());
            for (int i = 0; i < movieArray.length(); i++) {
                // These are the values that will be collected.
                String original_title;
                String poster_path;
                String overview;
                String release_date;
                Double vote_average;

                // Get the JSON object representing the movie
                JSONObject movieDetail = movieArray.getJSONObject(i);

                original_title = movieDetail.getString(TM_ORIGINAL_TITLE);
                poster_path = movieDetail.getString(TM_POSTER);
                overview = movieDetail.getString(TM_OVERVIEW);
                release_date = movieDetail.getString(TM_DATE);
                vote_average = movieDetail.getDouble(TM_VOTE_AVERAGE);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, original_title);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, poster_path);
                movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, release_date);
                movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);

//                cVVector.add(movieValues);
                Uri uri = Utility.getSortedUri(mContext);
                Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);
                long movie_id = MovieEntry.getIdFromUri(movieUri);
                ContentValues entryValues = new ContentValues();
                entryValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movie_id);
                mContext.getContentResolver().insert(uri, entryValues);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }
}
