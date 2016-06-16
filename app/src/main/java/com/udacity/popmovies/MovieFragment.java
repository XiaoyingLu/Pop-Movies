package com.udacity.popmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

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
 * Created by Administrator on 2016/6/4.
 */
public class MovieFragment extends Fragment {

    public static final String POSTER_PATH = "poster_path";
    public static final String ORIGINAL_TITLE = "original_title";
    public static final String RELEASE_DATE = "release_date";
    public static final String VOTE_AVERAGE = "vote_date";
    public static final String OVERVIEW = "overview";

    private ArrayAdapter<Movie> mMovieAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        if(itemId == R.id.popular){
            sharedPrefs.edit().putString(getString(R.string.order), getString(R.string.pref_popular_key)).commit();
            updateMovie();
            return true;
        }
        if(itemId == R.id.rated){
            sharedPrefs.edit().putString(getString(R.string.order), getString(R.string.pref_rated_key)).commit();
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    private void updateMovie() {
        FetchMovieTask fetchMovieTask = new FetchMovieTask();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String order = sharedPrefs.getString(getString(R.string.order), getString(R.string.pref_popular_key));
        fetchMovieTask.execute(order);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie);

        mMovieAdapter = new MovieListAdapter(getActivity(), new ArrayList<Movie>());

        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mMovieAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(POSTER_PATH, movie.getPoster_path())
                        .putExtra(ORIGINAL_TITLE, movie.getOriginal_title())
                        .putExtra(RELEASE_DATE, movie.getRelease_date())
                        .putExtra(VOTE_AVERAGE, movie.getVote_average())
                        .putExtra(OVERVIEW, movie.getOverview());
                startActivity(intent);
            }
        });
        return rootView;
    }

    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = MovieFragment.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

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

            String appId = "";

            try {
                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String APPID = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter(APPID, appId)
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
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
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
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr) {

            final String TM_RESULT = "results";
            final String TM_POSTER = "poster_path";
            final String TM_ORIGINAL_TITLE = "original_title";
            final String TM_OVERVIEW = "overview";
            final String TM_DATE = "release_date";
            final String TM_VOTE_AVERAGE = "vote_average";

            try {
                JSONObject movieJson = new JSONObject(movieJsonStr);
                JSONArray movieArray = movieJson.getJSONArray(TM_RESULT);

                ArrayList<Movie> movieList = new ArrayList<>();
//                String[] resultStr = new String[movieArray.length()];
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

                    movieList.add(movie);
                }
                return movieList;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> result) {
            if (null != result) {
                mMovieAdapter.clear();
                for (Movie movie : result) {
                    mMovieAdapter.add(movie);
                }
            }
        }
    }
}