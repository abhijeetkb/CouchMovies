package com.couchbase.couchmovies.movies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.couchbase.couchmovies.R;
import java.util.List;

public class MovieCardAdapter extends RecyclerView.Adapter<MovieCardAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public MovieCardAdapter(List<Movie> movies, OnMovieClickListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_card, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie, listener);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView posterImageView;

        MovieViewHolder(View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.moviePosterImageView);
        }

        void bind(final Movie movie, final OnMovieClickListener listener) {
            if (movie != null && movie.getImageUrl() != null) {
                Glide.with(itemView.getContext())
                        .load(movie.getImageUrl())
                        .placeholder(R.drawable.movie_placeholder)
                        .error(R.drawable.movie_placeholder)
                        .centerCrop()
                        .into(posterImageView);
            } else {
                posterImageView.setImageResource(R.drawable.movie_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null && movie != null) {
                    listener.onMovieClick(movie);
                }
            });
        }
    }
} 