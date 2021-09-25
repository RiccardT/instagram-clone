package com.example.instagramclone;

import android.content.Context;
import android.widget.Toast;

public class Utilities {

    public static void showToastMessage(String message, Context currentContext) {
        Toast toast = Toast.makeText(
                currentContext,
                message,
                Toast.LENGTH_SHORT
        );
        toast.show();
    }
}
