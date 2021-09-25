package com.example.instagramclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class UserFeedActivity extends AppCompatActivity {

    LinearLayout userFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);
        userFeed = findViewById(R.id.userFeed);
        Intent userListIntent = getIntent();
        String username = userListIntent.getStringExtra("username");
        setTitle(String.format("%s's Posts", username));
        getUsersFeedFromDatabaseGivenUsername(username);
    }

    private void getUsersFeedFromDatabaseGivenUsername(String username) {
        ParseQuery<ParseObject> userImageQuery = new ParseQuery<>("Image");
        userImageQuery.whereEqualTo("username", username);
        userImageQuery.orderByDescending("createdAt");
        userImageQuery.findInBackground((objects, e) -> {
            if (e == null && objects.size() > 0) {
                Log.i("IMAGE COUNT", String.valueOf(objects.size()));
                objects.forEach(userImageParseObject -> {
                    ImageView imageViewWithUsersImage = createImageViewWithParseImageObject(
                            userImageParseObject
                    );
                    userFeed.addView(imageViewWithUsersImage);
                });
            }
            else if (e != null) e.printStackTrace();
        });
    }

    private ImageView createImageViewWithParseImageObject(ParseObject parseImageObject) {
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        ParseFile imageFile = (ParseFile) parseImageObject.get("image");
        if (imageFile != null) {
            imageFile.getDataInBackground((data, e) -> {
                if (e == null && data != null) {
                    Log.i("IMAGE FILE RETRIEVAL", "Image received from server");
                    Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                    imageView.setImageBitmap(image);
                }
            });
        }
        else {
            Log.i("IMAGE FILE RETRIEVAL", "Image file was null");
            imageView.setImageResource(R.drawable.instagram_logo);
        }
        return imageView;
    }
}