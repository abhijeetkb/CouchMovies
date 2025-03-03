package com.couchbase.couchmovies.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.couchbase.couchmovies.R;
import com.couchbase.couchmovies.movies.MoviesActivity;
import com.couchbase.couchmovies.util.DatabaseManager;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput;
    EditText passwordInput;
    AppCompatImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);

        //TODO: Remove
        usernameInput.setText("demo@example.com");
        passwordInput.setText("Password@P1");

        //makes logging in easier for testing
        imageView = findViewById(R.id.imageViewLogo);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameInput.setText("demo@example.com");
                passwordInput.setText("Password@P1");
            }
        });
    }

    public void onLoginTapped(View view) {
        if (usernameInput.length() > 0 && passwordInput.length() > 0) {
            DatabaseManager dbMgr = DatabaseManager.getSharedInstance();
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            Context context = getApplicationContext();

            dbMgr.initCouchbaseLite(context);
            dbMgr.openOrCreateDatabaseForUser(context, username);

            DatabaseManager.startPushAndPullReplicationForCurrentUser(username, password, context);

            Intent intent = new Intent(getApplicationContext(), MoviesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
