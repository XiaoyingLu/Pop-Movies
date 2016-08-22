package com.udacity.popmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity implements MovieFragment.Callback,
        DetailFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private String mSort;
    private MovieNetHelper mMovieNetHelper;
    private boolean mTwoPane;
    private ScrollView mDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("MF", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        
        mSort = Utility.getPreferredSort(this);

        if (findViewById(R.id.fragment_detail) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp-land). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            mDetailFragment = (ScrollView) findViewById(R.id.fragment_detail);
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            mDetailFragment.setVisibility(View.GONE);

        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);

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

        if (itemId == R.id.popular) {
            sharedPrefs.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_popular_key)).commit();
//            mMovieNetHelper.updateMovies(Utility.getPreferredSort(this));
//            Loader loader = new Loader(this);
//            loader.startLoading();
            return true;
        }
        if (itemId == R.id.rated) {
            sharedPrefs.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_rated_key)).commit();
//            mMovieNetHelper.updateMovies(Utility.getPreferredSort(this));
//            Loader loader = new Loader(this);
//            loader.startLoading();
            return true;
        }
        if (itemId == R.id.action_setting) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(long id, Uri contentUri, MovieAdapter.MovieAdapterViewHolder vh) {

        if (mTwoPane) {
            mDetailFragment.setVisibility(View.VISIBLE);
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, contentUri);
            arguments.putLong(DetailFragment.MOVIE_ID, id);
            arguments.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri)
                    .putExtra(DetailFragment.MOVIE_ID, id);

            ActivityOptionsCompat activityOptions =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                            new Pair<View, String>(vh.mImageView, getString(R.string.detail_poster_transition_name)));
            ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sort = Utility.getPreferredSort(this);
//        if( sort != null && !sort.equals(mSort)){
////            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
//            MovieFragment ff = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
//            if (null != ff) {
//                ff.onSortChanged();
//            }
//            mSort = sort;
//        }
    }

    @Override
    public void onTitleLoaded(String original_title, String backdrop_path) {
        if (mTwoPane) {
            getSupportActionBar().setTitle(original_title);
        }
    }
}
