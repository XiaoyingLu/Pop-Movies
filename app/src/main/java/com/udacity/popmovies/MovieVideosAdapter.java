package com.udacity.popmovies;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Xiaoying on 8/18/16.
 */
public class MovieVideosAdapter extends RecyclerView.Adapter<MovieVideosAdapter.MovieVideosViewHolder>{

    private static final String YOUTUBE_THUMBNAIL = "https://img.youtube.com/vi/%s/mqdefault.jpg";
    private final Context mContext;
    private ArrayList<MovieVideo> mMovieVideos;
    private MovieVideosAdapterOnClickHandler mClickHandler;

    public static interface MovieVideosAdapterOnClickHandler {
        void onClick(MovieVideo movieVideo);
    }

    public class MovieVideosViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        private final ImageView mVideoThumbnail;

        public MovieVideosViewHolder(View itemView) {
            super(itemView);
            mVideoThumbnail = (ImageView) itemView.findViewById(R.id.movie_video_thumbnail);

            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            MovieVideo movieVideo = mMovieVideos.get(adapterPosition);
            mClickHandler.onClick(movieVideo);
        }

    }
    public MovieVideosAdapter(Context context, MovieVideosAdapterOnClickHandler vh) {
        mContext = context;
        mClickHandler = vh;
        mMovieVideos = new ArrayList<MovieVideo>();
    }

    public void setMovieVideos(ArrayList<MovieVideo> mMovieVideos) {
        this.mMovieVideos = mMovieVideos;
        notifyDataSetChanged();
    }

    @Override
    public MovieVideosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = R.layout.list_item_movie_video;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new MovieVideosViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(MovieVideosViewHolder holder, int position) {
        if (mMovieVideos == null) {
            return;
        }
        MovieVideo movieVideo = mMovieVideos.get(position);
        if (movieVideo.isYoutubeVideo()) {
            Picasso.with(mContext)
                    .load(String.format(YOUTUBE_THUMBNAIL, movieVideo.getKey()))
                    .placeholder(new ColorDrawable(mContext.getResources().getColor(R.color.accent_material_light)) )
                    .into(holder.mVideoThumbnail);
        }
    }

    @Override
    public int getItemCount() {
        if (mMovieVideos == null) {
            return 0;
        }
        return mMovieVideos.size();
    }
}
