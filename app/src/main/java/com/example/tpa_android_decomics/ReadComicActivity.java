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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.ComicChapter;
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

public class ReadComicActivity extends AppCompatActivity {

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    private DatabaseReference myDatabase;

    RecyclerView recView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_comic);

        Intent intent = getIntent();
        final String id = intent.getStringExtra("comicId");
        final String chapterId = intent.getStringExtra("position");
        final Context ctx = this;

        recView = findViewById(R.id.imageRecycler);

        myDatabase = FirebaseDatabase.getInstance().getReference();

        myDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> list = new ArrayList<>();
                for(DataSnapshot data : snapshot.child("comics").child(id).child("chapters").getChildren()){
                    list.add(data.child("image").getValue().toString());
                }
//
                ComicAdapter comAdapter = new ComicAdapter(ctx,list);
                recView.setAdapter(comAdapter);
                recView.setLayoutManager(new LinearLayoutManager(ctx));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.MyViewHolder>{

        ArrayList<String> images;
        Context ctx;

        String id;

        public ComicAdapter(Context ctx,ArrayList<String> images){
            this.ctx = ctx;
            this.images = images;
            this.id = id;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View view = inflater.inflate(R.layout.comic_image_card, parent, false);

            return  new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            LoadImageUrl loadImage = new LoadImageUrl(holder.iv);
            loadImage.execute(images.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView iv;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                iv = itemView.findViewById(R.id.imageRead);
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