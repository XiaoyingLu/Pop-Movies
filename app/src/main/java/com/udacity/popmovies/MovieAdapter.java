package com.udacity.popmovies;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private final FavoriteService favoriteService;

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder{
        public final ImageView mImageView;
        private final ImageButton mfavoriteButton;
        private MovieAdapterViewHolder vh;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            vh = this;
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_movie_imageview);
            mfavoriteButton = (ImageButton) itemView.findViewById(R.id.movie_favorite_button);

            mImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int adapterPosition = getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);
                    mClickHandler.onImageClick(mCursor.getLong(MovieFragment.COL_MOVIE_ID), vh);
                }
            });
            mfavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);
                    long id = mCursor.getLong(MovieFragment.COL_MOVIE_ID);
                    mfavoriteButton.setSelected(!favoriteService.isFavorite(id));
                    mClickHandler.onFavoriteClick(id);
                }
            });
        }
//
//        @Override
//        public void onClick(View view) {
//            int adapterPosition = getAdapterPosition();
//            mCursor.moveToPosition(adapterPosition);
//            mClickHandler.onFavoriteClick(mCursor.getLong(MovieFragment.COL_MOVIE_ID));
//            mClickHandler.onImageClick(mCursor.getLong(MovieFragment.COL_MOVIE_ID), this);
//        }

    }

    public static interface MovieAdapterOnClickHandler {

        void onFavoriteClick(long id);
        void onImageClick(long id, MovieAdapterViewHolder vh);
    }

    public MovieAdapter(Context context, MovieAdapterOnClickHandler dh, View emptyView) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
        mDataSetObserver = new NotifyingDataSetObserver();
        favoriteService = new FavoriteService(mContext);
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e("MA", "onCreateViewHolder");
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
        Log.e("MA", "onBindViewHolder");
        mCursor.moveToPosition(position);
        String poster_path = mCursor.getString(MovieFragment.COL_MOVIE_POSTER_PATH);
        long id = mCursor.getLong(MovieFragment.COL_MOVIE_ID);
        Log.e("MA", String.valueOf(id));
        holder.mfavoriteButton.setSelected(favoriteService.isFavorite(id));

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

//    public void selectView(RecyclerView.ViewHolder viewHolder) {
//        if (viewHolder instanceof MovieAdapterViewHolder) {
//            MovieAdapterViewHolder vfh = (MovieAdapterViewHolder) viewHolder;
//            vfh.onImageClick(vfh.itemView);
//        }
//    }

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
