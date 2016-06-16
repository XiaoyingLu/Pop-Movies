package com.udacity.popmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

/**
 * Created by Administrator on 2016/6/4.
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new DetailFragment())
                    .commit();
        }
    }
}
