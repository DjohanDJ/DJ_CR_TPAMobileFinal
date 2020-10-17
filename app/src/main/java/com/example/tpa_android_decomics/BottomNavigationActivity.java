package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationId);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerId, HomeFragment.getInstance()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = HomeFragment.getInstance();
                    break;

                case R.id.nav_search:
                    selectedFragment = new SearchFragment();
                    break;

                case R.id.nav_upload:
                    selectedFragment = UploadFragment.getInstance();
                    break;

                case R.id.nav_premium:
                    selectedFragment = PremiumFragment.getInstance();
                    break;

                case R.id.nav_profile:
                    ProfileFragment.selectedUser = null;
                    selectedFragment = new ProfileFragment();
                    break;

            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerId, selectedFragment).commit();

            return true;
        }
    };
}