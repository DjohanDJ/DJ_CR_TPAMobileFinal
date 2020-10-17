package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class OtherUserActivity extends AppCompatActivity {

    DatabaseReference myDatabase = null;
    User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);

        Intent intent = getIntent();
        final String uid = intent.getStringExtra("asdf");

        User user = new User();
        user.setUserId(uid);
        ProfileFragment.selectedUser = user;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerId, new ProfileFragment()).commit();
    }
}