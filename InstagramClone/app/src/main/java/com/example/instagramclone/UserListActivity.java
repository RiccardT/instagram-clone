package com.example.instagramclone;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    private ListView userListView;
    private ArrayList<String> usernames;
    private ArrayAdapter<String> usernamesArrayListAdapter;
    ActivityResultLauncher<Intent> photosActivityResultLauncher;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.share_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        setTitle(R.string.user_list_title);
        userListView = findViewById(R.id.userListView);
        usernames = new ArrayList<>();
        usernamesArrayListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                usernames
        );
        userListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent userFeedIntent = new Intent(
                    UserListActivity.this.getApplicationContext(),
                    UserFeedActivity.class
            );
            String clickedUsername = usernames.get(position);
            userFeedIntent.putExtra("username", clickedUsername);
            UserListActivity.this.startActivity(userFeedIntent);
        });
        photosActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        uploadImageToParseServer(selectedImage);

                    }
                }
        );
        fetchUsersFromDBIntoListView();
    }

    private void uploadImageToParseServer(Uri image) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(),
                    image
            );
            Log.i("IMAGE SELECTED", "Image was selected successfully");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            ParseFile file = new ParseFile("image.png", byteArray);
            ParseObject object = new ParseObject("Image");
            object.put("image", file);
            object.put("username", ParseUser.getCurrentUser().getUsername());
            object.saveInBackground(e -> {
                if (e == null) {
                    Utilities.showToastMessage(
                            "Image has been shared",
                            this
                    );
                    return;
                }
                e.printStackTrace();
                Log.i("IMAGE UPLOAD", "Failed to upload image to Parse");
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i("IMAGE UPLOAD", "Failed to select image from disk");
        }
    }

    private void fetchUsersFromDBIntoListView() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.addAscendingOrder("username");
        query.findInBackground((objects, e) -> {
            if (e == null && objects.size() > 0) {
                objects.forEach(user -> usernames.add(user.getUsername()));
                userListView.setAdapter(usernamesArrayListAdapter);
            }
            else if (e != null) e.printStackTrace();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.share && userHasNotGrantedFileAccessBefore()) {
            requestPermissions(
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    1
            );
        }
        else if (item.getItemId() == R.id.share && !userHasNotGrantedFileAccessBefore()) {
            openPhotoSelectionActivityForUserSelectedPhoto();
        }
        else if (item.getItemId() == R.id.logout) {
            ParseUser.logOut();
            Intent loginSignUpIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(loginSignUpIntent);

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean userHasNotGrantedFileAccessBefore() {
        return checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestForFileAccessWasValidAndGranted(requestCode, grantResults)) {
            openPhotoSelectionActivityForUserSelectedPhoto();
        }
    }

    private boolean requestForFileAccessWasValidAndGranted(int requestCode, int[] grantResults) {
        return requestCode == 1 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    private void openPhotoSelectionActivityForUserSelectedPhoto() {
        Intent photosIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        photosActivityResultLauncher.launch(photosIntent);
    }
}