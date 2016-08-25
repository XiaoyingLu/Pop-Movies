package com.udacity.popmovies;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Xiaoying on 8/25/16.
 */
public abstract class LoadScrollListener extends RecyclerView.OnScrollListener {

    private int lastVisibleItemPosition;

    public abstract void onLoad(int totalItemCount);

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        RecyclerView.LayoutManager layoutManager = recyclerView
                .getLayoutManager();
        lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        super.onScrolled(recyclerView, dx, dy);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        RecyclerView.LayoutManager layoutManager = recyclerView
                .getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if (visibleItemCount > 0 && totalItemCount > visibleItemCount
                && newState == RecyclerView.SCROLL_STATE_IDLE
                && lastVisibleItemPosition >= totalItemCount - 2 ){
            onLoad(totalItemCount);
        }
    }
}
