package com.example.tpa_android_decomics;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.Comic;
import com.example.tpa_android_decomics.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class ProfileFragment extends Fragment {

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    private DatabaseReference myDatabase;

    public static User currentUserSession = null;
    public static User selectedUser = null;

    ArrayList<Comic> comics = new ArrayList<>();

    RecyclerView recView;

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Indicates profile management
     */

    private TextView comicCountView, followersCountView, followingCountView, usernameView;
    private Integer counterComics = 0, counterFollowers = 0, counterFollowings = 0;
    private Button followButton, unFollowButton;
    private TextView followingTextBtn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initializeAttribute(view);
        getUserData(view);
        checkFollowButton();
        checkUnFollowButton();
        checkFollowTextButton();

        return view;
    }

    private void checkFollowButton() {
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followButton.setVisibility(View.INVISIBLE);
                unFollowButton.setVisibility(View.VISIBLE);
//                Toast.makeText(getActivity(), "Follow", Toast.LENGTH_SHORT).show();
                addFollowers(selectedUser.getUserId(), currentUserSession.getUserId());
            }
        });
    }

    private void checkUnFollowButton() {
        unFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unFollowButton.setVisibility(View.INVISIBLE);
                followButton.setVisibility(View.VISIBLE);
//                Toast.makeText(getActivity(), "Unfollow", Toast.LENGTH_SHORT).show();
                deleteFollowers(selectedUser.getUserId(), currentUserSession.getUserId());
            }
        });
    }

    private void checkFollowTextButton() {
        followingTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUser == null) {
                    startActivity(new Intent(getContext(), FollowingActivity.class));
                }
            }
        });
    }

    private void getUserData(final View view) {
        if (currentUserSession != null) {
            if (selectedUser == null) {
                unFollowButton.setVisibility(View.GONE);
                followButton.setVisibility(View.GONE);
                usernameView.setText(currentUserSession.getUsername());
                getComicsCount(currentUserSession);
                getFollowersCount(currentUserSession);
                getFollowingsCount(currentUserSession);
                getComicData(currentUserSession, view);
            } else {
                /**
                 * Get User yang dipilih by id
                 */
                myDatabase = FirebaseDatabase.getInstance().getReference();
                myDatabase.child("users").child(selectedUser.getUserId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedUser.setUsername(snapshot.child("username").getValue().toString());
                        usernameView.setText(selectedUser.getUsername());
                        getComicsCount(selectedUser);
                        getFollowersCount(selectedUser);
                        getFollowingsCount(selectedUser);
                        getComicData(selectedUser, view);
                        getFollowState(selectedUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }
    }

    public void addFollowers(String comicUID, final String currid){
        final DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("users").child(comicUID).child("followers");

        myDatabase.child("users").child(currid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("username").getValue().toString();
                userDatabase.child(currid).child("username").setValue(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteFollowers(String comicUID, String currid){
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("users").child(comicUID).child("followers").child(currid);
        userDatabase.removeValue();
    }

    private void getFollowState(final User user) {
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child("users").child(user.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("followers") && snapshot.child("followers").hasChild(currentUserSession.getUserId())){
                    followButton.setVisibility(View.INVISIBLE);
                    unFollowButton.setVisibility(View.VISIBLE);
                }else{
                    unFollowButton.setVisibility(View.INVISIBLE);
                    followButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getComicData(final User user, final View view) {
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child("comics").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comics.clear();
                for(DataSnapshot data : snapshot.getChildren()){
                    String userId = data.child("userId").getValue().toString();
                    if(userId.equals(user.getUserId())){
                        Comic com = new Comic();
                        com.setName(data.child("name").getValue().toString());
                        com.setImage(data.child("image").getValue().toString());
                        com.setId(data.getKey());
                        comics.add(com);
                    }
                }

                ComicAdapter recAdapter = new ComicAdapter(view.getContext(),comics);
                recView.setAdapter(recAdapter);
                recView.setLayoutManager(new GridLayoutManager(view.getContext(),3));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowingsCount(final User user) {
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                counterFollowings = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.child("followers").getChildren()) {
                        if (childSnapshot.getKey().equals(user.getUserId())) {
                            counterFollowings++;
                            break;
                        }
                    }
                }
                followingCountView.setText(counterFollowings.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowersCount(final User user) {
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                counterFollowers = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey().equals(user.getUserId())) {
                        for (DataSnapshot childSnapshot : dataSnapshot.child("followers").getChildren()) {
                            counterFollowers++;
                        }
                        break;
                    }
                }
                followersCountView.setText(counterFollowers.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getComicsCount(final User user) {
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child("comics").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                counterComics = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("userId").getValue().toString().equals(user.getUserId())) {
                        counterComics++;
                    }
                }
                comicCountView.setText(counterComics.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeAttribute(View view) {
        this.comicCountView = view.findViewById(R.id.comicCountId);
        this.followersCountView = view.findViewById(R.id.followerCountId);
        this.followingCountView = view.findViewById(R.id.followingCountId);
        this.usernameView = view.findViewById(R.id.usernameGetId);
        this.followButton = view.findViewById(R.id.buttonFollow);
        this.unFollowButton = view.findViewById(R.id.buttonUnfollow);
        this.recView = view.findViewById(R.id.recViewProfile);
        this.followingTextBtn = view.findViewById(R.id.textView10);
    }

    public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.MyViewHolder> {

        ArrayList<Comic> comics;
        Context ctx;
        final static String EXTRA_ID = "asd";

        public ComicAdapter(Context ctx,ArrayList<Comic> comics){
            this.ctx = ctx;
            this.comics = comics;
        }
        @NonNull
        @Override
        public ComicAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View view = inflater.inflate(R.layout.profile_comic_card, parent, false);


            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ComicAdapter.MyViewHolder holder, final int position) {
            holder.myText.setText(comics.get(position).getName());
            LoadImageUrl loadImage = new LoadImageUrl(holder.myImage);
            loadImage.execute(comics.get(position).getImage());

            holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), comics.get(position).getName(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(v.getContext(), ComicChapterActivity.class);
                    intent.putExtra("asd", comics.get(position).getId());
                    v.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return comics.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView myText;
            ImageView myImage;
            CardView cv;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                myText = itemView.findViewById(R.id.comicName);
                myImage = itemView.findViewById(R.id.comicImage);
                cv = itemView.findViewById(R.id.cardView);
            }
        }
    }

    private class LoadImageUrl extends AsyncTask<String, Void, Bitmap> {
        ImageView image;
        Bitmap retBitmap;

        public LoadImageUrl (ImageView image){
            this.image  = image;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String url = strings[0];
            Bitmap bitmap = null;
            try {
                InputStream inputStream = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            image.setImageBitmap(bitmap);
            retBitmap = bitmap;
        }
    }


}