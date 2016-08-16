package com.udacity.popmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MovieFragment.Callback{

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private String mSort;
    private MovieNetHelper mMovieNetHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (null != toolbar) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        mSort = Utility.getPreferredSort(this);

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_movie, new MovieFragment())
                    .commit();
        }

        mMovieNetHelper = new MovieNetHelper(this);
        mMovieNetHelper.updateMovies(Utility.getPreferredSort(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(itemId == R.id.popular){
            sharedPrefs.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_popular_key)).commit();
//            mMovieNetHelper.updateMovies(Utility.getPreferredSort(this));
//            Loader loader = new Loader(this);
//            loader.startLoading();
            return true;
        }
        if(itemId == R.id.rated){
            sharedPrefs.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_rated_key)).commit();
//            mMovieNetHelper.updateMovies(Utility.getPreferredSort(this));
//            Loader loader = new Loader(this);
//            loader.startLoading();
            return true;
        }
        if(itemId == R.id.action_setting){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri contentUri, MovieAdapter.MovieAdapterViewHolder vh) {

        Intent intent = new Intent(this, DetailActivity.class)
                .setData(contentUri);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sort = Utility.getPreferredSort(this);
        if( sort != null && !sort.equals(mSort)){
//            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            MovieFragment ff = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
            if (null != ff) {
                ff.onSortChanged();
            }
            mSort = sort;
        }
    }
}
