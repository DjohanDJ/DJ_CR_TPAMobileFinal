package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.Comic;
import com.example.tpa_android_decomics.models.ComicChapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ComicChapterActivity extends AppCompatActivity {


    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    private DatabaseReference myDatabase;
    private DatabaseReference userDatabase;

    RecyclerView recView;
    ImageView img;
    TextView title;
    TextView author;
    TextView rating;
    TextView genre;
    TextView desc;
    Button follow;
    Boolean followed = false;
    Button rate;
    RatingBar bar;
    Boolean isRated = false;
    TextView errText;
    View line;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    ArrayList<ComicChapter> comChaps = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_chapter);

        final Context ctx = this;


        Intent intent = getIntent();
        final String uid = intent.getStringExtra("asd");
        String comicUID;
        final String currid = ProfileFragment.currentUserSession.getUserId();

        recView = findViewById(R.id.recycleView);
        img = findViewById(R.id.imageView2);
        title = findViewById(R.id.textView);
        author = findViewById(R.id.textView2);
        rating = findViewById(R.id.textView11);
        genre = findViewById(R.id.textView9);
        desc = findViewById(R.id.textView8);
        follow = findViewById(R.id.button);
        errText = findViewById(R.id.errorText);
        line = findViewById(R.id.view2);
//        rate =  findViewById(R.id.button2);
        bar = findViewById(R.id.ratingBar2);




        myDatabase = FirebaseDatabase.getInstance().getReference();

        myDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comChaps.clear();
                final String comicUID = snapshot.child("comics").child(uid).child("userId").getValue().toString();
                checkFollowing(comicUID,currid);

                title.setText(snapshot.child("comics").child(uid).child("name").getValue().toString());
                desc.setText(snapshot.child("comics").child(uid).child("description").getValue().toString());
                genre.setText(snapshot.child("comics").child(uid).child("genre").getValue().toString());
                author.setText(snapshot.child("users").child(comicUID).child("username").getValue().toString());

                LoadImageUrl loadImage = new LoadImageUrl(img);
                loadImage.execute(snapshot.child("comics").child(uid).child("image").getValue().toString());

//                myDatabase.child("users").child(snapshot.child("userId").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        author.setText(snapshot.child("username").getValue().toString());
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

                int count = 0;
                int total = 0;
                for(DataSnapshot dataChild : snapshot.child("comics").child(uid).child("rating").getChildren()) {
                    count++;
                    total = total + Integer.parseInt(dataChild.child("ratingValue").getValue().toString());
                }
                float totalRating = 0;
                totalRating = (float)total / (float)count;
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                final String s  = String.valueOf(df.format(totalRating));
                rating.setText(s);

                for(DataSnapshot data : snapshot.child("comics").child(uid).child("chapters").getChildren()){
                    ComicChapter chap = new ComicChapter();
                    chap.setName(data.child("name").getValue().toString());
                    chap.setId(data.getKey());

                    comChaps.add(chap);
                }

                String premium = snapshot.child("comics").child(uid).child("premium").getValue().toString();

                if(premium.equals("true")){
                    if(!firebaseAuth.getCurrentUser().isEmailVerified() && !comicUID.equals(ProfileFragment.currentUserSession.getUserId())){
//                        rate.setVisibility(View.GONE);
                        bar.setVisibility(View.GONE);
                        errText.setVisibility(View.VISIBLE);
                        recView.setVisibility(View.GONE);
                        line.setVisibility(View.GONE);
                    }
                }


                folllowingSetup(ctx, comicUID,currid);
                checkRating(uid);
                setRatingBar(uid, currid);

                ComicAdapter comAdapter = new ComicAdapter(ctx,comChaps, uid);
                recView.setAdapter(comAdapter);
                recView.setLayoutManager(new LinearLayoutManager(ctx));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void setRatingBar(final String comicId, final String currId){
//        rate =  findViewById(R.id.button2);
        bar = findViewById(R.id.ratingBar2);
//        bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//            @Override
//            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//
//            }
//        });
        bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                float value = bar.getRating();
                int val = (int)value;

                userDatabase = FirebaseDatabase.getInstance().getReference("comics").child(comicId).child("rating").child(currId);
                userDatabase.child("ratingValue").setValue(val);

                if(isRated){
                    isRated = true;
                }else{
                    isRated = true;
                }
            }
        });

//        rate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                float value = bar.getRating();
//                int val = (int)value;
//
//                userDatabase = FirebaseDatabase.getInstance().getReference("comics").child(comicId).child("rating").child(currId);
//                userDatabase.child("ratingValue").setValue(val);
//                Toast.makeText(ComicChapterActivity.this, "Send rate success", Toast.LENGTH_SHORT).show();
//                if(isRated){
//                    isRated = true;
//                }else{
//                    isRated = true;
//                }
//            }
//        });

    }

    public void checkRating(final String comicId){
        myDatabase.child("comics").child(comicId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currId = ProfileFragment.currentUserSession.getUserId();
                if(snapshot.hasChild("rating") && snapshot.child("rating").hasChild(currId)){
                    isRated = true;
                    String ratingVal = snapshot.child("rating").child(currId).child("ratingValue").getValue().toString();
                    bar.setRating(Float.parseFloat(ratingVal));

                }else{
                    isRated = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void folllowingSetup(final Context ctx, final String comicUID, final String currid){
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(followed){
                    followed = false;
                    follow.setText("Follow");
                    deleteFollowers(comicUID,currid);
                    follow.setTextColor(Color.BLACK);
                }else{
                    followed = true;
                    follow.setText("Followed");
                    follow.setTextColor(Color.GRAY);
                    addFollowers(comicUID,currid);

                }
            }
        });
    }

    public void checkFollowing(final String comicUID, final String currid){
        if(comicUID.equals(currid))
        {
            follow.setVisibility(View.GONE);
            return;
        }

        myDatabase.child("users").child(comicUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("followers") && snapshot.child("followers").hasChild(currid)){
                    followed = true;
                    follow.setText("Followed");
                    follow.setTextColor(Color.GRAY);
                }else{
                    followed = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addFollowers(String comicUID, final String currid){
        userDatabase = FirebaseDatabase.getInstance().getReference("users").child(comicUID).child("followers");

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
        userDatabase = FirebaseDatabase.getInstance().getReference("users").child(comicUID).child("followers").child(currid);
        userDatabase.removeValue();
    }

    public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.MyViewHolder>{

        ArrayList<ComicChapter> chapters;
        Context ctx;
        final static String EXTRA_ID = "asd";
        String id;

        public ComicAdapter(Context ctx,ArrayList<ComicChapter> chapters, String id){
            this.ctx = ctx;
            this.chapters = chapters;
            this.id = id;
        }

        @NonNull
        @Override
        public ComicAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View view = inflater.inflate(R.layout.comic_chapter_row, parent, false);


            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ComicAdapter.MyViewHolder holder, final int position) {
            holder.myText.setText(chapters.get(position).getName());

            holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ctx, chapters.get(position).getName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(v.getContext(), ReadComicActivity.class);
                    intent.putExtra("comicId", id);

                    intent.putExtra("position", chapters.get(position).getId());

                    v.getContext().startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return chapters.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView myText;
            CardView cv;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                myText = itemView.findViewById(R.id.chapterName);
                cv = itemView.findViewById(R.id.comChapCard);
            }
        }
    }

    private class LoadImageUrl extends AsyncTask<String, Void, Bitmap>  {
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

