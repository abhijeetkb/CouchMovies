package com.couchbase.couchmovies.movies;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.QueryChange;
import com.couchbase.lite.QueryChangeListener;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.couchmovies.R;
import com.couchbase.couchmovies.util.DatabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesActivity extends AppCompatActivity implements MovieCardAdapter.OnMovieClickListener {
    private ImageView featuredMovieBanner;
    private TextView featuredMovieTitle;
    private Map<String, RecyclerView> categoryRecyclerViews;
    private Map<String, MovieCardAdapter> categoryAdapters;
    private Map<String, List<Movie>> categoryMovies;
    private Map<String, Query> categoryQueries;
    private Map<String, ListenerToken> queryListenerTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        
        setTitle("CouchMovies");
        
        initializeViews();
        setupRecyclerViews();
        setupQueries();
    }

    private void initializeViews() {
        featuredMovieBanner = findViewById(R.id.featuredMovieBanner);
        featuredMovieTitle = findViewById(R.id.featuredMovieTitle);
        
        categoryRecyclerViews = new HashMap<>();
        categoryAdapters = new HashMap<>();
        categoryMovies = new HashMap<>();
        categoryQueries = new HashMap<>();
        queryListenerTokens = new HashMap<>();

        // Initialize RecyclerViews for each category
        categoryRecyclerViews.put("top10", findViewById(R.id.topMoviesRecyclerView));
        categoryRecyclerViews.put("continue", findViewById(R.id.continueWatchingRecyclerView));
        categoryRecyclerViews.put("action", findViewById(R.id.actionMoviesRecyclerView));
        categoryRecyclerViews.put("animated", findViewById(R.id.animatedMoviesRecyclerView));
        categoryRecyclerViews.put("blockbuster", findViewById(R.id.blockbusterMoviesRecyclerView));
    }

    private void setupRecyclerViews() {
        for (Map.Entry<String, RecyclerView> entry : categoryRecyclerViews.entrySet()) {
            String category = entry.getKey();
            RecyclerView recyclerView = entry.getValue();

            // Initialize movie list for category
            List<Movie> movies = new ArrayList<>();
            categoryMovies.put(category, movies);

            // Setup horizontal layout
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);

            // Setup adapter
            MovieCardAdapter adapter = new MovieCardAdapter(movies, this);
            categoryAdapters.put(category, adapter);
            recyclerView.setAdapter(adapter);
        }
    }

    private void setupQueries() {
        Database database = DatabaseManager.getUserProfileDatabase();

        // Setup queries for each category
        setupCategoryQuery(database, "top10", Expression.property("isTop10").equalTo(Expression.booleanValue(true)));
        setupCategoryQuery(database, "continue", Expression.property("watchProgress").greaterThan(Expression.intValue(0)));
        setupCategoryQuery(database, "action", Expression.property("category").equalTo(Expression.string("action")));
        setupCategoryQuery(database, "animated", Expression.property("category").equalTo(Expression.string("animated")));
        setupCategoryQuery(database, "blockbuster", Expression.property("category").equalTo(Expression.string("blockbuster")));

        // Setup featured movie query
        Query featuredQuery = QueryBuilder
                .select(
                    SelectResult.property("title"),
                    SelectResult.property("genre"),
                    SelectResult.property("description"),
                    SelectResult.property("year"),
                    SelectResult.property("director"),
                    SelectResult.property("rating"),
                    SelectResult.property("imageUrl"),
                    SelectResult.property("category"),
                    SelectResult.property("watchProgress"),
                    SelectResult.property("isTop10"),
                    SelectResult.property("top10Rank")
                )
                .from(DataSource.database(database))
                .where(Expression.property("featured").equalTo(Expression.booleanValue(true)))
                .limit(Expression.intValue(1));

        try {
            ResultSet rows = featuredQuery.execute();
            Result row = rows.next();
            if (row != null) {
                updateFeaturedMovie(row);
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void setupCategoryQuery(Database database, String category, Expression whereExpression) {
        Query query = QueryBuilder
                .select(
                    SelectResult.property("title"),
                    SelectResult.property("genre"),
                    SelectResult.property("description"),
                    SelectResult.property("year"),
                    SelectResult.property("director"),
                    SelectResult.property("rating"),
                    SelectResult.property("imageUrl"),
                    SelectResult.property("category"),
                    SelectResult.property("watchProgress"),
                    SelectResult.property("isTop10"),
                    SelectResult.property("top10Rank")
                )
                .from(DataSource.database(database))
                .where(Expression.property("type").equalTo(Expression.string("movie"))
                        .and(whereExpression));

        categoryQueries.put(category, query);

        ListenerToken token = query.addChangeListener(new QueryChangeListener() {
            @Override
            public void changed(QueryChange change) {
                updateMoviesList(category, change.getResults());
            }
        });

        queryListenerTokens.put(category, token);

        try {
            ResultSet rows = query.execute();
            updateMoviesList(category, rows);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void updateMoviesList(String category, ResultSet rows) {
        List<Movie> movies = categoryMovies.get(category);
        movies.clear();

        Result row;
        while ((row = rows.next()) != null) {
            movies.add(createMovieFromResult(row));
        }

        runOnUiThread(() -> categoryAdapters.get(category).notifyDataSetChanged());
    }

    private Movie createMovieFromResult(Result row) {
        try {
            return new Movie(
                row.getString("title"),
                row.getString("genre"),
                row.getString("description"),
                row.getInt("year"),
                row.getString("director"),
                (float) row.getDouble("rating"),
                row.getString("imageUrl"),
                row.getString("category"),
                row.getInt("watchProgress"),
                row.getBoolean("isTop10"),
                row.getInt("top10Rank")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateFeaturedMovie(Result row) {
        Movie movie = createMovieFromResult(row);
        runOnUiThread(() -> {
            featuredMovieTitle.setText(movie.getTitle());
            Glide.with(this)
                    .load(movie.getImageUrl())
                    .into(featuredMovieBanner);
        });
    }

    @Override
    public void onMovieClick(Movie movie) {
        Toast.makeText(this, "Selected: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove all query listeners
        for (Map.Entry<String, ListenerToken> entry : queryListenerTokens.entrySet()) {
            String category = entry.getKey();
            ListenerToken token = entry.getValue();
            Query query = categoryQueries.get(category);
            if (query != null && token != null) {
                query.removeChangeListener(token);
            }
        }
    }
} 