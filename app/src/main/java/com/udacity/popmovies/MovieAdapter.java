package com.udacity.popmovies;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Administrator on 2016/6/4.
 */
public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final String PICTURE_BASE_URL = "http://image.tmdb.org/t/p/";
    final String PICTURE_SIZE = "w185";
    private static final int FOOTER_VIEW = 1;
    private final Context mContext;
    private final MovieAdapterOnClickHandler mClickHandler;
    private final View mEmptyView;
    private Cursor mCursor;
    private final DataSetObserver mDataSetObserver;
    private final FavoriteService favoriteService;

    public static final int PULL_TO_LOAD_MORE = 0;
    public static final int IS_LOADING = 1;
    public static final int LOAD_ERROR = 2;
    public static final int NO_MORE_LOAD = 3;
    public int load_more_status = IS_LOADING;
    private boolean mIsFooterVisible = false;

    // Define a view holder for Footer view
    public class FooterViewHolder extends RecyclerView.ViewHolder {

        private final ProgressBar mFooterProgressbar;
        private final TextView mFooterLoadText;
        private final View mFooterView;

        public FooterViewHolder(View itemView) {
            super(itemView);
            mFooterView = itemView.findViewById(R.id.load_footer_view);
            mFooterProgressbar = (ProgressBar) itemView.findViewById(R.id.load_footer_progressbar);
            mFooterLoadText = (TextView) itemView.findViewById(R.id.load_footer_text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (load_more_status == LOAD_ERROR) {
                        // Click to reload
                        mClickHandler.onFooterClick();
                    }
                }
            });
        }
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder {
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

    }

    public static interface MovieAdapterOnClickHandler {

        void onFavoriteClick(long id);

        void onImageClick(long id, MovieAdapterViewHolder vh);

        void onFooterClick();
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == FOOTER_VIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_footer_view, parent, false);
            return new FooterViewHolder(view);
        }
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie, parent, false);
        view.setFocusable(true);
        return new MovieAdapterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            FooterViewHolder vh = (FooterViewHolder) holder;
            if (!mIsFooterVisible){
                vh.mFooterView.getLayoutParams().height = 0;
            } else {
                int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                vh.mFooterView.measure(w, h);
                int height = vh.mFooterView.getMeasuredHeight();
                vh.mFooterView.getLayoutParams().height = height;
                if (load_more_status == IS_LOADING) {
                    vh.mFooterLoadText.setText(R.string.footer_isloading);
                    vh.mFooterProgressbar.setVisibility(View.VISIBLE);
                } else if (load_more_status == LOAD_ERROR) {
                    vh.mFooterLoadText.setText(R.string.footer_load_error);
                    vh.mFooterProgressbar.setVisibility(View.INVISIBLE);
                } else if (load_more_status == NO_MORE_LOAD) {
                    vh.mFooterLoadText.setText(R.string.footer_no_more);
                    vh.mFooterProgressbar.setVisibility(View.INVISIBLE);
                } else {
                    vh.mFooterLoadText.setText(R.string.footer_pull_to_load);
                    vh.mFooterProgressbar.setVisibility(View.INVISIBLE);
                }
            }
        } else if (holder instanceof MovieAdapterViewHolder) {
            MovieAdapterViewHolder vh = (MovieAdapterViewHolder) holder;

            mCursor.moveToPosition(position);
            String poster_path = mCursor.getString(MovieFragment.COL_MOVIE_POSTER_PATH);
            long id = mCursor.getLong(MovieFragment.COL_MOVIE_ID);
            vh.mfavoriteButton.setSelected(favoriteService.isFavorite(id));

            Picasso.with(mContext)
                    .load(PICTURE_BASE_URL + PICTURE_SIZE + "//" + poster_path)
                    .into(vh.mImageView);

            // this enables better animations. even if we lose state due to a device rotation,
            // the animator can use this to re-find the original view
            ViewCompat.setTransitionName(vh.mImageView, "posterView" + position);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mCursor.getCount()) {
            return FOOTER_VIEW;
        }
        return super.getItemViewType(position);
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

    public void updateFooterView(int status) {
        load_more_status = status;
        notifyDataSetChanged();
    }

    public void setFooterVisibility(boolean isFooterVisible) {
        mIsFooterVisible = isFooterVisible;
    }
}
