package com.udacity.popmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Xiaoying on 8/2/16.
 */
public class FetchMovieTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    public static ArrayList<Movie> mMovies;
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
            // If the code didn't successfully get the weather data, there's no point in attemping
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
        final String TM_POSTER = "poster_path";
        final String TM_ORIGINAL_TITLE = "original_title";
        final String TM_OVERVIEW = "overview";
        final String TM_DATE = "release_date";
        final String TM_VOTE_AVERAGE = "vote_average";

        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TM_RESULT);

            mMovies = new ArrayList<>();
            for (int i = 0; i < movieArray.length(); i++) {
                Movie movie = new Movie();
                String poster_path;
                String original_title;
                String overview;
                String release_date;
                String vote_average;

                JSONObject movieDetail = movieArray.getJSONObject(i);

                poster_path = movieDetail.getString(TM_POSTER);
                original_title = movieDetail.getString(TM_ORIGINAL_TITLE);
                overview = movieDetail.getString(TM_OVERVIEW);
                release_date = movieDetail.getString(TM_DATE);
                vote_average = movieDetail.getString(TM_VOTE_AVERAGE);

                movie.setPoster_path(poster_path);
                movie.setOriginal_title(original_title);
                movie.setOverview(overview);
                movie.setRelease_date(release_date);
                movie.setVote_average(vote_average);

                mMovies.add(movie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
