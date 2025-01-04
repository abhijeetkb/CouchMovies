package com.couchbase.couchmovies.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.couchbase.lite.Blob;
import com.couchbase.couchmovies.R;
import com.couchbase.couchmovies.login.LoginActivity;
import com.couchbase.couchmovies.util.DatabaseManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity
        extends AppCompatActivity
        implements UserProfileContract.View {

    private UserProfileContract.UserActionsListener mActionListener;

    EditText nameInput;
    EditText emailInput;
    EditText addressInput;
    ImageView imageView;

    ActivityResultLauncher<Intent> mainActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                                imageView.setImageBitmap(bitmap);
                            } catch (IOException ex) {
                                Log.i("SelectPhoto", ex.getMessage());
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        addressInput = findViewById(R.id.addressInput);
        imageView = findViewById(R.id.imageView);

        mActionListener = new UserProfilePresenter(this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActionListener.fetchProfile();
            }
        });
    }

    public void onUploadPhotoTapped(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mainActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    public void onLogoutTapped(View view) {
        DatabaseManager.stopAllReplicationForCurrentUser();
        DatabaseManager.getSharedInstance().closeDatabaseForUser();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void onSaveTapped(View view) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", nameInput.getText().toString());
        profile.put("email", emailInput.getText().toString());
        profile.put("address", addressInput.getText().toString());
        profile.put("type", "user");
        byte[] imageViewBytes = getImageViewBytes();

        if (imageViewBytes != null) {
            profile.put("imageData", new com.couchbase.lite.Blob("image/jpeg", imageViewBytes));
        }

        mActionListener.saveProfile(profile);
        Toast.makeText(this, "Successfully updated profile!", Toast.LENGTH_SHORT).show();
    }

    private byte[] getImageViewBytes() {
        byte[] imageBytes = null;

        BitmapDrawable bmDrawable = (BitmapDrawable) imageView.getDrawable();

        if (bmDrawable != null) {
            Bitmap bitmap = bmDrawable.getBitmap();

            if (bitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imageBytes = baos.toByteArray();
            }
        }
        return imageBytes;
    }

    @Override
    public void showProfile(Map<String, Object> profile) {
        nameInput.setText((String)profile.get("name"));
        emailInput.setText((String)profile.get("email"));
        addressInput.setText((String)profile.get("address"));

        Blob imageBlob = (Blob)profile.get("imageData");

        if (imageBlob != null) {
            Drawable d = Drawable.createFromStream(imageBlob.getContentStream(), "res");
            imageView.setImageDrawable(d);
        }
    }
}
