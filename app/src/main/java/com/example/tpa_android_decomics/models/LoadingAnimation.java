package com.example.tpa_android_decomics.models;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.tpa_android_decomics.R;

public class LoadingAnimation {

    private Activity myActivity;
    private AlertDialog dialog;

    public LoadingAnimation(Activity myActivity) {
        this.myActivity = myActivity;
    }

    public void startLoading() {
        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);

        LayoutInflater inflater = myActivity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_animation, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }


}
