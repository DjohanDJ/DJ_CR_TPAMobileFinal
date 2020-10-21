package com.example.tpa_android_decomics;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpa_android_decomics.models.Comic;
import com.example.tpa_android_decomics.models.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SearchedUserFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<User> listUser;
    private ArrayList<User> listUserFull;
    public static SearchedUserFragment.MyNewRecAdapter recAdapter;

    public SearchedUserFragment(ArrayList<User> listUser, ArrayList<User> listUserFull) {
        this.listUser = listUser;
        this.listUserFull = listUserFull;
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

        recAdapter = new SearchedUserFragment.MyNewRecAdapter(view.getContext(), listUser);
        recyclerView.setAdapter(recAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));


        return view;
    }

    public class MyNewRecAdapter extends RecyclerView.Adapter<SearchFragment.MyViewHolder> implements Filterable {

        private Context context;
        private ArrayList<User> comicsList;

        public MyNewRecAdapter(@NonNull Context context, ArrayList<User> comicsList) {
            this.context = context;
            this.comicsList = comicsList;
        }

        @NonNull
        @Override
        public SearchFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.card_search_row, parent, false);

            return new SearchFragment.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchFragment.MyViewHolder holder, final int position) {
            holder.myTitle.setText(comicsList.get(position).getUsername());
//            SearchedComicFragment.LoadImageUrl loadImage = new SearchedComicFragment.LoadImageUrl(holder.myImage);
//            loadImage.execute(comicsList.get(position).getImage());
            holder.myImage.setImageResource(R.drawable.ic_undraw_profile_pic_ic5t);

            final String title = holder.myTitle.getText().toString();

            holder.cvA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), comicsList.get(position).getUserId(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(v.getContext(), OtherUserActivity.class);
                    intent.putExtra("asdf", comicsList.get(position).getUserId());
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
                ArrayList<User> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(listUserFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (User item : listUserFull) {
                        if (item.getUsername().toLowerCase().contains(filterPattern)) {
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
                listUser.clear();
                listUser.addAll((ArrayList) results.values);
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
