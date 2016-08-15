package com.udacity.popmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

public class MainActivity extends AppCompatActivity implements MovieFragment.Callback{

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private String mSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSort = Utility.getPreferredSort(this);

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieFragment())
                    .commit();
        }

        MovieNetHelper movieNetHelper = new MovieNetHelper(this);
        movieNetHelper.updateMovies(Utility.getPreferredSort(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
            MovieFragment ff = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.container);
            if (null != ff) {
                ff.onSortChanged();
            }
            mSort = sort;
        }
    }
}
