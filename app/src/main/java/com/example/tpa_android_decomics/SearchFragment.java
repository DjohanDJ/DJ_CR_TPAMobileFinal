package com.example.tpa_android_decomics;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.Comic;
import com.example.tpa_android_decomics.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private static SearchFragment instance = null;
    private SearchView mySearchView;
    public static ArrayList<Comic> comicLists;
    private ArrayList<Comic> comicListsFull;
    private DatabaseReference myDatabase;
    private RecyclerView recyclerView;
    TabLayout tabLay;
    ViewPager viewPager;

    public static ArrayList<User> userLists;
    private ArrayList<User> userListsFull;

//    public static SearchFragment getInstance(){
//        if(instance == null){
//            instance = new SearchFragment();
//        }
//        return instance;
//    }

    public SearchFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
//        ctx = view.getContext();
        // TODO nanti mau buat TabLayout
        tabLay = (TabLayout) view.findViewById(R.id.tabLayout);
        tabLay.addTab(tabLay.newTab().setText(getResources().getString(R.string.comik)));
        tabLay.addTab(tabLay.newTab().setText(getResources().getString(R.string.user)));
        tabLay.setTabGravity(tabLay.GRAVITY_FILL);


        comicLists = new ArrayList<Comic>();
        comicListsFull = new ArrayList<Comic>();
        userLists = new ArrayList<User>();
        userListsFull = new ArrayList<User>();
        recyclerView = view.findViewById(R.id.recView);
        mySearchView = view.findViewById(R.id.searchViewId);
        myDatabase = FirebaseDatabase.getInstance().getReference();



        myDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comicLists.clear();
                comicListsFull.clear();
                for (DataSnapshot dataSnapshot : snapshot.child("comics").getChildren()) {
                    Comic comic = new Comic();
                    comic.setId(dataSnapshot.getKey());
                    comic.setName(dataSnapshot.child("name").getValue().toString());
                    comic.setImage(dataSnapshot.child("image").getValue().toString());
                    comicLists.add(comic);
                    comicListsFull.add(comic);
                }

                userLists.clear();
                userListsFull.clear();
                for (DataSnapshot dataSnapshot : snapshot.child("users").getChildren()) {
                    if (!dataSnapshot.getKey().equals(ProfileFragment.currentUserSession.getUserId())) {
                        User user = new User();
                        user.setUserId(dataSnapshot.getKey());
                        user.setUsername(dataSnapshot.child("username").getValue().toString());
                        userLists.add(user);
                        userListsFull.add(user);
                    }
                }

                final PagerAdapter adapter = new SearchPagerAdapter(getChildFragmentManager(), tabLay.getTabCount(), comicLists, comicListsFull, userLists, userListsFull);
                viewPager = (ViewPager) view.findViewById(R.id.pager);
                viewPager.setAdapter(adapter);
                viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLay));
//                ((SearchPagerAdapter)adapter).setComicLists(comicLists, comicListsFull);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        myDatabase.child("users").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                userLists.clear();
//                userListsFull.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    if (!dataSnapshot.getKey().equals(ProfileFragment.currentUserSession.getUserId())) {
//                        User user = new User();
//                        user.setUserId(dataSnapshot.getKey());
//                        user.setUsername(dataSnapshot.child("username").getValue().toString());
//                        userLists.add(user);
//                        userListsFull.add(user);
//                    }
//                }
//
//                ((SearchPagerAdapter)adapter).setUserLists(userLists, userListsFull);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

//        myDatabase.child("users").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                userLists.clear();
//                userListsFull.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    if (!dataSnapshot.getKey().equals(ProfileFragment.currentUserSession.getUserId())) {
//                        User user = new User();
//                        user.setUserId(dataSnapshot.getKey());
//                        user.setUsername(dataSnapshot.child("username").getValue().toString());
//                        userLists.add(user);
//                        userListsFull.add(user);
//                    }
//                }
////                viewPager = (ViewPager) view.findViewById(R.id.pager);
////                final PagerAdapter adapter = new SearchPagerAdapter(getChildFragmentManager(), tabLay.getTabCount(), comicLists, comicListsFull, userLists, userListsFull);
////                viewPager.setAdapter(adapter);
////                viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLay));
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

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

//        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });

        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                SearchedComicFragment.recAdapter.getFilter().filter(newText);
                SearchedUserFragment.recAdapter.getFilter().filter(newText);
//                if (!newText.isEmpty()) {
//                }

                return false;
            }
        });

        return view;
    }

    class SearchPagerAdapter extends FragmentPagerAdapter {
        private int numTabs;
        private ArrayList<Comic> listComic;
        private ArrayList<Comic> listComicFull;

        private ArrayList<User> userLists;
        private ArrayList<User> userListsFull;



        public SearchPagerAdapter(FragmentManager fm, int numTabs, ArrayList<Comic> listComic, ArrayList<Comic> listComicFull, ArrayList<User> userLists, ArrayList<User> userListsFull) {
            super(fm);
            this.numTabs = numTabs;
            this.listComic = listComic;
            this.listComicFull = listComicFull;
            this.userLists = userLists;
            this.userListsFull = userListsFull;
        }

//        public void setComicLists(ArrayList<Comic> listComic, ArrayList<Comic> listComicFull) {
//            this.listComic = listComic;
//            this.listComicFull = listComicFull;
//            this.notifyDataSetChanged();
//        }
//
//        public void setUserLists(ArrayList<User> userLists, ArrayList<User> userListsFull) {
//            this.userLists = userLists;
//            this.userListsFull = userListsFull;
//            this.notifyDataSetChanged();
//        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    SearchedComicFragment t1 = new SearchedComicFragment(listComic, listComicFull);
                    return t1;
                case 1:
                    SearchedUserFragment t2 = new SearchedUserFragment(userLists, userListsFull);
                    return t2;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return numTabs;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView myTitle;
        ImageView myImage;
        CardView cvA;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myTitle = itemView.findViewById(R.id.titleRow);
            myImage = itemView.findViewById(R.id.imageRow);
            cvA = itemView.findViewById(R.id.cardView);
        }
    }

}