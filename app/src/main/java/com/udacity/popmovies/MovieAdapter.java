package com.udacity.popmovies;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Administrator on 2016/6/4.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    final String PICTURE_BASE_URL = "http://image.tmdb.org/t/p/";
    final String PICTURE_SIZE = "w185";
    private final Context mContext;
    private final MovieAdapterOnClickHandler mClickHandler;
    private final View mEmptyView;
    private Cursor mCursor;
    private final DataSetObserver mDataSetObserver;

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final ImageView mImageView;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_movie_imageview);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(mCursor.getLong(MovieFragment.COL_MOVIE_ID), this);
        }

    }
    public static interface MovieAdapterOnClickHandler {

        void onClick(long id, MovieAdapterViewHolder vh);
    }
    public MovieAdapter(Context context, MovieAdapterOnClickHandler dh, View emptyView) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = R.layout.list_item_movie;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new MovieAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String poster_path = mCursor.getString(MovieFragment.COL_MOVIE_POSTER_PATH);

        Picasso.with(mContext)
                .load(PICTURE_BASE_URL + PICTURE_SIZE + "//" + poster_path)
                .into(holder.mImageView);

        // this enables better animations. even if we lose state due to a device rotation,
        // the animator can use this to re-find the original view
        ViewCompat.setTransitionName(holder.mImageView, "posterView" + position);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (null != mCursor && null != mDataSetObserver) {
            mCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (null != mCursor) {
            if (null != mDataSetObserver) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            notifyDataSetChanged();
        } else {
            notifyDataSetChanged();
        }
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof MovieAdapterViewHolder) {
            MovieAdapterViewHolder vfh = (MovieAdapterViewHolder) viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

    private class NotifyingDataSetObserver extends DataSetObserver {

        public NotifyingDataSetObserver() {
            super();
        }

        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            notifyDataSetChanged();
        }
    }
}
