package com.example.tpa_android_decomics;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.Comic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class SearchedComicFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<Comic> listComic;
    private ArrayList<Comic> listComicFull;
    public static MyNewRecAdapter recAdapter;

    public SearchedComicFragment(ArrayList<Comic> listComic, ArrayList<Comic> listComicFull) {
        this.listComic = listComic;
        this.listComicFull = listComicFull;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top_comic, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recView);

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        recAdapter = new MyNewRecAdapter(view.getContext(), listComic);
        recyclerView.setAdapter(recAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));


        return view;
    }

    public class MyNewRecAdapter extends RecyclerView.Adapter<SearchFragment.MyViewHolder> implements Filterable {

        private Context context;
        private ArrayList<Comic> comicsList;

        public MyNewRecAdapter(@NonNull Context context, ArrayList<Comic> comicsList) {
            this.context = context;
            this.comicsList = comicsList;
        }

        @NonNull
        @Override
        public SearchFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.top_comic_row, parent, false);

            return new SearchFragment.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchFragment.MyViewHolder holder, final int position) {
            holder.myTitle.setText(comicsList.get(position).getName());
            LoadImageUrl loadImage = new LoadImageUrl(holder.myImage);
            loadImage.execute(comicsList.get(position).getImage());

            final String title = holder.myTitle.getText().toString();

            holder.cvA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), comicsList.get(position).getId(), Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(v.getContext(), ComicChapterActivity.class);
                    intent.putExtra("asd", comicsList.get(position).getId());
                    v.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return comicsList.size();
        }

        @Override
        public Filter getFilter() {
            return exampleFilter;
        }

        private Filter exampleFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<Comic> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(listComicFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Comic item : listComicFull) {
                        if (item.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listComic.clear();
                listComic.addAll((ArrayList) results.values);
                notifyDataSetChanged();
            }
        };
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