package com.example.tpa_android_decomics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.tpa_android_decomics.models.Comic;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    String title[] = {"One Punch Man", "Boku no Hero Academia", "Cheese in the trap", "Solo Leveling", "Demon Slayer"};
    int images[] = {R.drawable.insta,R.drawable.insta,R.drawable.insta,R.drawable.insta,R.drawable.insta};
    public ArrayList<Comic> recComic =  new ArrayList<>();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    private DatabaseReference myDatabase;

    private static HomeFragment instance = null;

    private HomeFragment(){}

    public static HomeFragment getInstance(){
        if(instance == null){
            instance = new HomeFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recView);
        TabLayout tabLay = (TabLayout) view.findViewById(R.id.tabLayout);
        tabLay.addTab(tabLay.newTab().setText(getResources().getString(R.string.rom)));
        tabLay.addTab(tabLay.newTab().setText(getResources().getString(R.string.fan)));
        tabLay.addTab(tabLay.newTab().setText(getResources().getString(R.string.hor)));
        tabLay.setTabGravity(tabLay.GRAVITY_FILL);

        //set recommended comic lists
        myDatabase = FirebaseDatabase.getInstance().getReference();

        myDatabase.child("comics").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recComic.clear();
                ArrayList<Comic> tempRecComic = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Comic comic = new Comic();
                    String uid = dataSnapshot.getKey();
                    comic.setId(uid);
                    comic.setName(dataSnapshot.child("name").getValue().toString());
                    comic.setImage(dataSnapshot.child("image").getValue().toString());
                    int count = 0;
                    int total = 0;
                    for(DataSnapshot dataChild : dataSnapshot.child("rating").getChildren()) {
                        count++;
                        total = total + Integer.parseInt(dataChild.child("ratingValue").getValue().toString());
                    }
                    comic.setRating((float)total / (float)count);
                    tempRecComic.add(comic);
                }
                Collections.sort(tempRecComic, new Comparator<Comic>() {
                    @Override
                    public int compare(Comic o1, Comic o2) {
                        if(Float.compare(o1.getRating(), o2.getRating()) > 0){
                            return -1;
                        }else{
                            return 1;
                        }
                    }
                });

                int size = 0;
                recComic.clear();
                for(Comic temp : tempRecComic){
                    recComic.add(temp);
                    size++;
                    if (size == 5) break;
                }


                RecComicRecyclerAdapter recAdapter = new RecComicRecyclerAdapter(view.getContext(),recComic);
                recyclerView.setAdapter(recAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


//        adapter view pager

        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        final PagerAdapter adapter = new TopComicPagerAdapter(getChildFragmentManager(), tabLay.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLay));
        


        tabLay.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition()) ;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLay));

        return view;
    }

}