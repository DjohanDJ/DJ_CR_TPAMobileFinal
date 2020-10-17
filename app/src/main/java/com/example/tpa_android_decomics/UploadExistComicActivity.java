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
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UploadExistComicActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference myDatabase;
    private ArrayList<Comic> comicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_exist_comic);
        final Context ctx = this;
        recyclerView = findViewById(R.id.recViewComic);
        comicList = new ArrayList<>();
        myDatabase = FirebaseDatabase.getInstance().getReference();
        myDatabase.child("comics").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comicList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("userId").getValue().toString().equals(ProfileFragment.currentUserSession.getUserId())) {
                        Comic newComic = new Comic();
                        newComic.setUserId(dataSnapshot.getKey());
                        newComic.setName(dataSnapshot.child("name").getValue().toString());
                        newComic.setImage(dataSnapshot.child("image").getValue().toString());
                        comicList.add(newComic);
                    }
                }

                UploadExistComicActivity.ComicAdapter comicAdapter = new UploadExistComicActivity.ComicAdapter(ctx, comicList);
                recyclerView.setAdapter(comicAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
//                recView.setLayoutManager(new GridLayoutManager(view.getContext(),3));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class ComicAdapter extends RecyclerView.Adapter<UploadExistComicActivity.ComicAdapter.MyViewHolder> {

        ArrayList<Comic> comics;
        Context ctx;
        final static String EXTRA_ID = "asd";

        public ComicAdapter(Context ctx, ArrayList<Comic> comics){
            this.ctx = ctx;
            this.comics = comics;
        }

        @NonNull
        @Override
        public UploadExistComicActivity.ComicAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View view = inflater.inflate(R.layout.top_comic_row, parent, false);


            return new UploadExistComicActivity.ComicAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UploadExistComicActivity.ComicAdapter.MyViewHolder holder, final int position) {
            holder.myText.setText(comics.get(position).getName());
            LoadImageUrl loadImage = new LoadImageUrl(holder.myImage);
            loadImage.execute(comics.get(position).getImage());

            holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), UploadChapterActivity.class);
                    intent.putExtra("asdf", comics.get(position).getUserId());
                    v.getContext().startActivity(intent);
                }
            });
        }

        public void deleteFollowers(String comicUID, String currid){
            DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("users").child(comicUID).child("followers").child(currid);
            userDatabase.removeValue();
        }

        @Override
        public int getItemCount() {
            return comics.size();
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