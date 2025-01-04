package com.couchbase.couchmovies.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.couchbase.couchmovies.R;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private Context context;

    public MovieAdapter(List<Movie> movies, Context context) {
        this.movies = movies;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.titleTextView.setText(movie.getTitle());
        holder.genreTextView.setText(movie.getGenre());
        holder.yearTextView.setText(String.valueOf(movie.getYear()));
        holder.directorTextView.setText(movie.getDirector());
        holder.ratingTextView.setText(String.format("%.1f", movie.getRating()));

        // Load image using Glide with error handling
        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.movie_placeholder)
                .error(R.drawable.movie_placeholder);

        Glide.with(context)
                .load(movie.getImageUrl())
                .apply(requestOptions)
                .into(holder.posterImageView);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;
        TextView titleTextView;
        TextView genreTextView;
        TextView yearTextView;
        TextView directorTextView;
        TextView ratingTextView;

        MovieViewHolder(View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.moviePosterImageView);
            titleTextView = itemView.findViewById(R.id.movieTitleTextView);
            genreTextView = itemView.findViewById(R.id.movieGenreTextView);
            yearTextView = itemView.findViewById(R.id.movieYearTextView);
            directorTextView = itemView.findViewById(R.id.movieDirectorTextView);
            ratingTextView = itemView.findViewById(R.id.movieRatingTextView);
        }
    }
} 