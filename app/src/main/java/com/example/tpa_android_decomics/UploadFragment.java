package com.example.tpa_android_decomics;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;


public class UploadFragment extends Fragment {



    private UploadFragment() {
        // Required empty public constructor
    }

    private static UploadFragment instance = null;

    public static UploadFragment getInstance(){
        if(instance == null){
            instance = new UploadFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private Button switchNewComicBtn, switchExistComicBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        initializeAttribute(view);
        checkSwitchButton();
        return view;
    }

    private void checkSwitchButton() {
        switchNewComicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UploadNewComicActivity.class));
            }
        });
        switchExistComicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UploadExistComicActivity.class));
            }
        });
    }

    private void initializeAttribute(View view) {
        this.switchNewComicBtn = view.findViewById(R.id.switchNewComic);
        this.switchExistComicBtn = view.findViewById(R.id.switchExistComic);
    }


}