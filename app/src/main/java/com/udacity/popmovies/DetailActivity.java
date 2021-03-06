package com.udacity.popmovies;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Administrator on 2016/6/4.
 */
public class DetailActivity extends AppCompatActivity implements DetailFragment.Callback{

    private CollapsingToolbarLayout mCollapsingToolbar;
    private ImageView mBackDrop;
    final String PICTURE_BASE_URL = "http://image.tmdb.org/t/p/";
    final String BACKDROP_SIZE = "w780";
    private String mMovie;
    private static final String MOVIE_SHARE_HASHTAG = " #PopMoviesApp";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mMovie + MOVIE_SHARE_HASHTAG);
            item.setIntent(shareIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mBackDrop = (ImageView) findViewById(R.id.back_drop);

        if (null == savedInstanceState) {

            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
            arguments.putLong(DetailFragment.MOVIE_ID, getIntent().getLongExtra(DetailFragment.MOVIE_ID, 0));
            arguments.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);
            if(!fragment.isAdded()) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_detail, fragment)
                        .commit();
            }
            // Being here means we are in animation mode
            supportPostponeEnterTransition();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onTitleLoaded(String original_title, String backdrop_path) {
        mCollapsingToolbar.setTitle(original_title);

        Picasso.with(this).load(PICTURE_BASE_URL + BACKDROP_SIZE + "//" + backdrop_path)
                .into(mBackDrop, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) mBackDrop.getDrawable()).getBitmap();
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette palette) {
                                applyPalette(palette);
                            }
                        });
                    }

                    @Override
                    public void onError() {

                    }
                });

        mMovie = original_title;
    }

    private void applyPalette(Palette palette) {
        int primaryDark = getResources().getColor(R.color.primary_dark);
        int primary = getResources().getColor(R.color.primary);
        mCollapsingToolbar.setContentScrimColor(palette.getMutedColor(primary));
        mCollapsingToolbar.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
//        updateBackground((FloatingActionButton) findViewById(R.id.fab), palette);
        supportStartPostponedEnterTransition();
    }

//    private void updateBackground(FloatingActionButton fab, Palette palette) {
//        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));
//        int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.accent));
//
//        fab.setRippleColor(lightVibrantColor);
//        fab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
//    }


//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        getSupportFragmentManager().putFragment(savedInstanceState);
//    }
}
