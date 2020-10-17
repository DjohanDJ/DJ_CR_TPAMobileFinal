package com.example.tpa_android_decomics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.tpa_android_decomics.models.Comic;
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


public class TopComic extends Fragment {

    RecyclerView recyclerView;
    String title[] = {"One Punch Man", "Boku no Hero Academia", "Cheese in the trap", "Solo Leveling", "Demon Slayer"};
    int images[] = {R.drawable.insta,R.drawable.insta,R.drawable.insta,R.drawable.insta,R.drawable.insta};
    public ArrayList<Comic> topList = new ArrayList<>();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    private DatabaseReference myDatabase;
    private String genre = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public TopComic(String genre){
        this.genre = genre;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_top_comic, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recView);

        //Set array list and adapter

        myDatabase = FirebaseDatabase.getInstance().getReference();

        myDatabase.child("comics").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topList.clear();
                ArrayList<Comic> tempList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Comic comic = new Comic();
                    comic.setId(dataSnapshot.getKey());
                    comic.setName(dataSnapshot.child("name").getValue().toString());
                    comic.setImage(dataSnapshot.child("image").getValue().toString());
                    comic.setGenre(dataSnapshot.child("genre").getValue().toString());
                    int count = 0;
                    int total = 0;
                    for(DataSnapshot dataChild : dataSnapshot.child("rating").getChildren()) {
                        count++;
                        total = total + Integer.parseInt(dataChild.child("ratingValue").getValue().toString());
                    }
                    comic.setRating((float)total / (float)count);
                    tempList.add(comic);
                }

                Collections.sort(tempList, new Comparator<Comic>() {
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
                topList.clear();
                for(Comic temp : tempList){
                    if(temp.getGenre().equalsIgnoreCase(genre)){
                        topList.add(temp);
                        size++;
                        if(size == 5) break;
                    }


                }

                TopComicRecyclerAdapter adapter = new TopComicRecyclerAdapter(view.getContext(),topList);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        TopComicRecyclerAdapter adapter = new TopComicRecyclerAdapter(view.getContext(),topList);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });



        return view;
    }
}