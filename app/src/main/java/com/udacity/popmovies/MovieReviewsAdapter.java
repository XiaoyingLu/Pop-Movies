package com.udacity.popmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Xiaoying on 8/18/16.
 */
public class MovieReviewsAdapter extends RecyclerView.Adapter<MovieReviewsAdapter.MovieReviewsViewHolder>{

    private static final String YOUTUBE_THUMBNAIL = "https://img.youtube.com/vi/%s/mqdefault.jpg";
    private final Context mContext;
    private ArrayList<MovieReview> mMovieReviews;
    private MovieReviewsAdapterOnClickHandler mClickHandler;

    public static interface MovieReviewsAdapterOnClickHandler {
        void onClick(MovieReview movieReview);
    }

    public class MovieReviewsViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        private final TextView mReviewContent;
        private final TextView mReviewAuthor;

        public MovieReviewsViewHolder(View itemView) {
            super(itemView);
            mReviewContent = (TextView) itemView.findViewById(R.id.movie_review_content);
            mReviewAuthor = (TextView) itemView.findViewById(R.id.movie_review_author);

            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            MovieReview movieReview = mMovieReviews.get(adapterPosition);
            mClickHandler.onClick(movieReview);
        }

    }
    public MovieReviewsAdapter(Context context, MovieReviewsAdapterOnClickHandler vh) {
        mContext = context;
        mClickHandler = vh;
        mMovieReviews = new ArrayList<MovieReview>();
    }

    public void setMovieReviews(ArrayList<MovieReview> mMovieReviews) {
        this.mMovieReviews = mMovieReviews;
        notifyDataSetChanged();
    }

    @Override
    public MovieReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = R.layout.list_item_movie_review;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new MovieReviewsViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(MovieReviewsViewHolder holder, int position) {
        if(null == mMovieReviews){
            return;
        }
        MovieReview movieReview = mMovieReviews.get(position);
        holder.mReviewContent.setText(movieReview.getContent());
        holder.mReviewAuthor.setText(movieReview.getAuthor());
    }

    @Override
    public int getItemCount() {
        if (mMovieReviews == null) {
            return 0;
        }
        return mMovieReviews.size();
    }
}
