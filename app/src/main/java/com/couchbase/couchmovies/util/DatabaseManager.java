package com.couchbase.couchmovies.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseChange;
import com.couchbase.lite.DatabaseChangeListener;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentReplication;
import com.couchbase.lite.DocumentReplicationListener;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Function;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorActivityLevel;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.ReplicatorType;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.URLEndpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static Database userprofileDatabase;
    private static String userProfileDbName = "userprofile";
    private static DatabaseManager instance = null;

    public static String appServicesEndpoint = "wss://8j33envdhdqqahi.apps.cloud.couchbase.com:4984/userprofileurl";

    private ListenerToken listenerToken;
    public String currentUser = null;

    private static Replicator replicator;
    private static ListenerToken replicatorListenerToken;

    protected DatabaseManager() {
    }

    public static DatabaseManager getSharedInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static Database getUserProfileDatabase() {
        return userprofileDatabase;
    }

    public String getCurrentUserDocId() {
        return "user::" + currentUser;
    }

    public void initCouchbaseLite(Context context) {
        CouchbaseLite.init(context);
    }

    public void openOrCreateDatabaseForUser(Context context, String username) {
        DatabaseConfiguration config = new DatabaseConfiguration();
        config.setDirectory(String.format("%s/%s", context.getFilesDir(), username));

        currentUser = username;

        try {
            userprofileDatabase = new Database(userProfileDbName, config);
            registerForDatabaseChanges();
            initializeSampleMovies();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void initializeSampleMovies() {
        try {
            // Check if movies already exist
            Query query = QueryBuilder
                    .select(SelectResult.expression(Function.count(Expression.all())))
                    .from(DataSource.database(userprofileDatabase))
                    .where(Expression.property("type").equalTo(Expression.string("movie")));

            long movieCount = query.execute().next().getLong(0);

            if (movieCount == 0) {
                // Sample movies data with categories and image URLs
                Movie[] sampleMovies = {
                    // Top 10 Movies
                    new Movie("The Shawshank Redemption", "Drama", 1994, "Frank Darabont", 9.3f,
                            "https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg",
                            "Two imprisoned men bond over a number of years.", "drama", 0, true, 1),
                    new Movie("The Godfather", "Crime, Drama", 1972, "Francis Ford Coppola", 9.2f,
                            "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
                            "The aging patriarch of an organized crime dynasty transfers control to his son.", "crime", 0, true, 2),

                    // Action Movies
                    new Movie("The Dark Knight", "Action, Crime, Drama", 2008, "Christopher Nolan", 9.0f,
                            "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
                            "Batman fights the menace known as the Joker.", "action", 0, false, 0),
                    new Movie("Inception", "Action, Adventure, Sci-Fi", 2010, "Christopher Nolan", 8.8f,
                            "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
                            "A thief who steals corporate secrets through dream-sharing technology.", "action", 0, false, 0),

                    // Animated Movies
                    new Movie("Toy Story", "Animation, Adventure, Comedy", 1995, "John Lasseter", 8.3f,
                            "https://image.tmdb.org/t/p/w500/uXDfjJbdP4ijW5hWSBrPrlKpxab.jpg",
                            "A story of toys that come to life.", "animated", 0, false, 0),
                    new Movie("Spirited Away", "Animation, Adventure, Family", 2001, "Hayao Miyazaki", 8.6f,
                            "https://image.tmdb.org/t/p/w500/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg",
                            "A young girl enters a mysterious world.", "animated", 0, false, 0),

                    // Holiday Movies
                    new Movie("Home Alone", "Comedy, Family", 1990, "Chris Columbus", 7.7f,
                            "https://image.tmdb.org/t/p/w500/9wSbe4CwObACCQvaUVhWQyLR5Vz.jpg",
                            "A kid defends his home from burglars during Christmas.", "holiday", 0, false, 0),
                    new Movie("Elf", "Comedy, Family, Fantasy", 2003, "Jon Favreau", 7.0f,
                            "https://image.tmdb.org/t/p/w500/oOleziEempUPu96jkGs0Pj6tKxj.jpg",
                            "A human raised as an elf spreads Christmas cheer.", "holiday", 0, false, 0),

                    // Blockbuster Movies
                    new Movie("Avatar", "Action, Adventure, Fantasy", 2009, "James Cameron", 7.8f,
                            "https://image.tmdb.org/t/p/w500/jRXYjXNq0Cs2TcJjLkki24MLp7u.jpg",
                            "A paraplegic marine dispatched to the moon Pandora.", "blockbuster", 0, false, 0),
                    new Movie("Titanic", "Drama, Romance", 1997, "James Cameron", 7.9f,
                            "https://image.tmdb.org/t/p/w500/9xjZS2rlVxm8SFx8kPC3aIGCOYQ.jpg",
                            "A seventeen-year-old aristocrat falls in love aboard the Titanic.", "blockbuster", 0, false, 0),
                            
                    // Additional Top 10 Movies
                    new Movie("Pulp Fiction", "Crime, Drama", 1994, "Quentin Tarantino", 8.9f,
                            "https://image.tmdb.org/t/p/w500/fIE3lAGcZDV1G6XM5KmuWnNsPp1.jpg",
                            "Various interconnected stories of criminals in Los Angeles.", "crime", 0, true, 3),
                    new Movie("The Matrix", "Action, Sci-Fi", 1999, "Lana and Lilly Wachowski", 8.7f,
                            "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
                            "A computer programmer discovers a mysterious world.", "action", 0, true, 4),
                            
                    // Additional Action Movies
                    new Movie("Mad Max: Fury Road", "Action, Adventure", 2015, "George Miller", 8.1f,
                            "https://image.tmdb.org/t/p/w500/8tZYtuWezp8JbcsvHYO0O46tFbo.jpg",
                            "In a post-apocalyptic world, a woman rebels against a tyrannical ruler.", "action", 0, false, 0),
                            
                    // Additional Animated Movies
                    new Movie("The Lion King", "Animation, Adventure, Drama", 1994, "Roger Allers", 8.5f,
                            "https://image.tmdb.org/t/p/w500/sKCr78MXSLixwmZ8DyJLrpMsd15.jpg",
                            "A young lion prince flees his kingdom after the murder of his father.", "animated", 0, false, 0),
                            
                    // Additional Holiday Movies
                    new Movie("The Polar Express", "Animation, Adventure, Family", 2004, "Robert Zemeckis", 6.6f,
                            "https://image.tmdb.org/t/p/w500/eOoCzH0MqeGr2taUZO4SwG416PF.jpg",
                            "A young boy embarks on a magical adventure to the North Pole.", "holiday", 0, false, 0)
                };

                // Save movies to database
                for (Movie movie : sampleMovies) {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("type", "movie");
                    properties.put("title", movie.title);
                    properties.put("genre", movie.genre);
                    properties.put("year", movie.year);
                    properties.put("director", movie.director);
                    properties.put("rating", movie.rating);
                    properties.put("imageUrl", movie.imageUrl);
                    properties.put("description", movie.description);
                    properties.put("category", movie.category);
                    properties.put("watchProgress", movie.watchProgress);
                    properties.put("isTop10", movie.isTop10);
                    properties.put("top10Rank", movie.top10Rank);
                    properties.put("featured", movie.top10Rank == 1);

                    String docId = "movie::" + movie.title.toLowerCase().replace(" ", "_");
                    MutableDocument document = new MutableDocument(docId, properties);
                    userprofileDatabase.save(document);
                }
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private static class Movie {
        String title;
        String genre;
        int year;
        String director;
        float rating;
        String imageUrl;
        String description;
        String category;
        int watchProgress;
        boolean isTop10;
        int top10Rank;

        Movie(String title, String genre, int year, String director, float rating,
              String imageUrl, String description, String category, int watchProgress,
              boolean isTop10, int top10Rank) {
            this.title = title;
            this.genre = genre;
            this.year = year;
            this.director = director;
            this.rating = rating;
            this.imageUrl = imageUrl;
            this.description = description;
            this.category = category;
            this.watchProgress = watchProgress;
            this.isTop10 = isTop10;
            this.top10Rank = top10Rank;
        }
    }

    private void registerForDatabaseChanges() {
        listenerToken = userprofileDatabase.addChangeListener(new DatabaseChangeListener() {
            @Override
            public void changed(final DatabaseChange change) {
                if (change != null) {
                    for(String docId : change.getDocumentIDs()) {
                        Document doc = userprofileDatabase.getDocument(docId);
                        if (doc != null) {
                            Log.i("DatabaseChangeEvent", "Document was added/updated");
                        }
                        else {
                            Log.i("DatabaseChangeEvent","Document was deleted");
                        }
                    }
                }
            }
        });
    }

    public static void startPushAndPullReplicationForCurrentUser(String username, String password, Context context) {
        URI url = null;
        try {
            url = new URI(String.format("%s", appServicesEndpoint));
            System.out.println("URL: " + url.toString());
        } catch (URISyntaxException e) {
            System.out.println("URL exception: " + url.toString());
            e.printStackTrace();
        }

        ReplicatorConfiguration config = new ReplicatorConfiguration(userprofileDatabase, new URLEndpoint(url));
        config.setType(ReplicatorType.PUSH_AND_PULL);
        config.setContinuous(true);
        config.setAuthenticator(new BasicAuthenticator(username, password.toCharArray()));

        replicator = new Replicator(config);

        replicatorListenerToken = replicator.addChangeListener(new ReplicatorChangeListener() {
            @Override
            public void changed(ReplicatorChange change) {
                if (change.getReplicator().getStatus().getActivityLevel().equals(ReplicatorActivityLevel.IDLE)) {
                    Log.e("Replication Comp Log", "Scheduler Completed");
                }
                if (change.getReplicator().getStatus().getActivityLevel().equals(ReplicatorActivityLevel.STOPPED)
                        || change.getReplicator().getStatus().getActivityLevel().equals(ReplicatorActivityLevel.OFFLINE)) {
                    Log.e("Rep Scheduler  Log", "ReplicationTag Stopped");
                }
            }
        });

        replicatorListenerToken = replicator.addDocumentReplicationListener(new DocumentReplicationListener() {
            @Override
            public void replication(@NonNull DocumentReplication replication) {
                Log.e("Replicated Document ", "Outside");
                replication.getDocuments().listIterator().forEachRemaining(i -> {
                    Log.e("Replicated Document ", i.getID());
                });
            }
        });

        replicator.start();
    }

    public static void stopAllReplicationForCurrentUser() {
        replicator.removeChangeListener(replicatorListenerToken);
        replicator.stop();
    }

    public void closeDatabaseForUser() {
        try {
            if (userprofileDatabase != null) {
                deregisterForDatabaseChanges();
                userprofileDatabase.close();
                userprofileDatabase = null;
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void deregisterForDatabaseChanges() {
        if (listenerToken != null) {
            userprofileDatabase.removeChangeListener(listenerToken);
        }
    }
}
