package com.couchbase.couchmovies.profile;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.QueryChange;
import com.couchbase.lite.QueryChangeListener;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.couchmovies.util.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

public class UserProfilePresenter implements UserProfileContract.UserActionsListener {
    private UserProfileContract.View mUserProfileView;

    public UserProfilePresenter(UserProfileContract.View mUserProfileView) {
        this.mUserProfileView = mUserProfileView;
    }

    public void fetchProfile() {
        Database database = DatabaseManager.getUserProfileDatabase();
        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();

        Query query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Meta.id.equalTo(Expression.string(docId)));

        query.addChangeListener(new QueryChangeListener() {
            @Override
            public void changed(QueryChange change) {
                ResultSet rows = change.getResults();
                Result row = null;
                Map<String, Object> profile = new HashMap<>();

                profile.put("email", DatabaseManager.getSharedInstance().currentUser);

                while ((row = rows.next()) != null) {
                    Dictionary dictionary = row.getDictionary("userprofile");

                    if (dictionary != null) {
                        profile.put("name", dictionary.getString("name"));
                        profile.put("address", dictionary.getString("address"));
                        profile.put("imageData", dictionary.getBlob("imageData"));
                    }
                }

                mUserProfileView.showProfile(profile);
            }
        });
    }

    public void saveProfile(Map<String, Object> profile) {
        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();
        Database database = DatabaseManager.getUserProfileDatabase();

        MutableDocument mutableDocument = new MutableDocument(docId, profile);

        try {
            database.save(mutableDocument);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
