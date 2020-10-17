package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.Comic;
import com.example.tpa_android_decomics.models.ComicChapter;
import com.example.tpa_android_decomics.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class FollowingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference myDatabase;
    private ArrayList<User> followingUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        final Context ctx = this;
        recyclerView = findViewById(R.id.recViewFollowing);
        followingUserList = new ArrayList<>();
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingUserList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.child("followers").getChildren()) {
                        if (childSnapshot.getKey().equals(ProfileFragment.currentUserSession.getUserId())) {
                            childSnapshot.getKey();
                            User newUser = new User();
                            newUser.setUserId(dataSnapshot.getKey());
                            newUser.setUsername(dataSnapshot.child("username").getValue().toString());
                            followingUserList.add(newUser);
                            break;
                        }
                    }
                }

                UserAdapter userAdapter = new UserAdapter(ctx, followingUserList);
                recyclerView.setAdapter(userAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
//                recView.setLayoutManager(new GridLayoutManager(view.getContext(),3));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

        ArrayList<User> users;
        Context ctx;
        final static String EXTRA_ID = "asd";

        public UserAdapter(Context ctx, ArrayList<User> users){
            this.ctx = ctx;
            this.users = users;
        }

        @NonNull
        @Override
        public FollowingActivity.UserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View view = inflater.inflate(R.layout.following_user_row, parent, false);


            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
            holder.myText.setText(users.get(position).getUsername());
            holder.myImage.setImageResource(R.drawable.ic_baseline_account_circle_24);
//            ProfileFragment.LoadImageUrl loadImage = new ProfileFragment.LoadImageUrl(holder.myImage);
//            loadImage.execute(comics.get(position).getImage());

            holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), OtherUserActivity.class);
                    intent.putExtra("asdf", users.get(position).getUserId());
                    v.getContext().startActivity(intent);
                }
            });

            holder.followingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), users.get(position).getUsername(), Toast.LENGTH_LONG).show();
                    Toast.makeText(v.getContext(), "Unfollow", Toast.LENGTH_SHORT).show();
                    deleteFollowers(users.get(position).getUserId(), ProfileFragment.currentUserSession.getUserId());
                }
            });
        }

        public void deleteFollowers(String comicUID, String currid){
            DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("users").child(comicUID).child("followers").child(currid);
            userDatabase.removeValue();
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView myText;
            ImageView myImage;
            CardView cv;
            Button followingBtn;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                myText = itemView.findViewById(R.id.titleRow);
                myImage = itemView.findViewById(R.id.imageRow);
                cv = itemView.findViewById(R.id.cardView);
                followingBtn = itemView.findViewById(R.id.followingId);
            }
        }
    }
}