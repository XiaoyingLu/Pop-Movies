package com.udacity.popmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity implements MovieFragment.Callback,
        DetailFragment.Callback, NavigationView.OnNavigationItemSelectedListener {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static final String BROADCAST_FAVORITE = "FavoriteBroadcast";
    private String mSort;
    private MovieNetHelper mMovieNetHelper;
    private boolean mTwoPane;
    private ScrollView mDetailFragment;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("MF", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mNavigationView.setNavigationItemSelectedListener(this);

        mSort = Utility.getPreferredSort(this);

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_movie, new MovieFragment())
                    .commit();
        }
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

//    @Override
//    protected void onResume() {
//        super.onResume();
//        String sort = Utility.getPreferredSort(this);
//        if( sort != null && !sort.equals(mSort)){
//            MovieFragment ff = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
//            if (null != ff) {
//                ff.onSortChanged();
//            }
//            mSort = sort;
//        }
//    }

    @Override
    public void onTitleLoaded(String original_title, String backdrop_path) {
        if (mTwoPane) {
            getSupportActionBar().setTitle(original_title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        switch (itemId) {
            case R.id.popular:
                sharedPrefs.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_popular_key)).commit();
                mMovieNetHelper.updateMovies(Utility.getPreferredSort(this));
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_movie, new MovieFragment())
                        .commit();
                return true;
            case R.id.rated:
                sharedPrefs.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_rated_key)).commit();
                mMovieNetHelper.updateMovies(Utility.getPreferredSort(this));
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_movie, new MovieFragment())
                        .commit();
                return true;
        }

        mNavigationView.getMenu().getItem(0).setChecked(true);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.drawer_home:
                mDrawerLayout.closeDrawers();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_movie, new MovieFragment())
                        .commit();
                return true;
            case R.id.drawer_favorite:
                mDrawerLayout.closeDrawers();

//                Bundle args = new Bundle();
//                args.putInt(MovieFragment.FAVORIE_LOADER, MovieFragment.MOVIE_FAVORITE_LOADER);
//                MovieFragment movieFragment = new MovieFragment();
//                movieFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_movie, new FavoriteFragment())
                        .commit();

//                Intent intent = new Intent(BROADCAST_FAVORITE);
//                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                return true;

        }
        return false;
    }
}
