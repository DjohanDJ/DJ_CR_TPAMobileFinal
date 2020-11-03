package com.example.tpa_android_decomics;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpa_android_decomics.models.Comic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class RecComicRecyclerAdapter extends RecyclerView.Adapter<RecComicRecyclerAdapter.MyViewHolder>{

    ArrayList<Comic> recComic;
    Context ctx;
    final static String EXTRA_ID = "asd";

    public RecComicRecyclerAdapter(Context ctx, ArrayList<Comic> recComic){
        this.ctx = ctx;
        this.recComic = recComic;
    }

    @NonNull
    @Override
    public RecComicRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.rec_comic_row, parent, false);

        return new RecComicRecyclerAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.myTitle.setText(recComic.get(position).getName());
        LoadImageUrl loadImage = new LoadImageUrl(holder.myImage);
        loadImage.execute(recComic.get(position).getImage());

        final String title = holder.myTitle.getText().toString();

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(v.getContext(), recComic.get(position).getId(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(v.getContext(), ComicChapterActivity.class);
                intent.putExtra(EXTRA_ID, recComic.get(position).getId());
                v.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return recComic.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView myTitle;
        ImageView myImage;
        CardView cv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myTitle = itemView.findViewById(R.id.titleRow);
            myImage = itemView.findViewById(R.id.imageRow);
            cv = itemView.findViewById(R.id.cardView);
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
