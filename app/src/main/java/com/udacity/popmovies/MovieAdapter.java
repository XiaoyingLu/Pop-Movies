package com.udacity.popmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/6/4.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    final String PICTURE_BASE_URL = "http://image.tmdb.org/t/p/";
    final String PICTURE_SIZE = "w185";
    private final Context mContext;
    private final MovieAdapterOnClickHandler mClickHandler;
    private ArrayList<Movie> mMovies;

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final ImageView mImageView;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_movie_imageview);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(adapterPosition, this);
        }

    }
    public static interface MovieAdapterOnClickHandler {

        void onClick(int position, MovieAdapterViewHolder vh);
    }
    public MovieAdapter(Context context, MovieAdapterOnClickHandler dh) {
        mContext = context;
        mClickHandler = dh;
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
        Movie movie = mMovies.get(position);
        Picasso.with(mContext)
                .load(PICTURE_BASE_URL + PICTURE_SIZE + "//" + movie.getPoster_path())
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        if (null == mMovies){
            return 0;
        }
        return mMovies.size();
    }

    public void swapMovie(ArrayList<Movie> movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    public ArrayList<Movie> getMovies() {
        return mMovies;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof MovieAdapterViewHolder) {
            MovieAdapterViewHolder vfh = (MovieAdapterViewHolder) viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }
}
