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
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.ComicChapter;
import com.example.tpa_android_decomics.models.Comment;
import com.example.tpa_android_decomics.models.ReplyComment;
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
    private DatabaseReference addCommentDatabase;
    private DatabaseReference dbCheck;

    RecyclerView recView;
    RecyclerView comView;

    Button addbtn;
    EditText commentDesc;
    ImageView heartLike, heartDislike,nextBtn;
    TextView chapterLike, chapterDislike;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_comic);


        Intent intent = getIntent();
        final String id = intent.getStringExtra("comicId");
        final String chapterId = intent.getStringExtra("position");
        final String position = intent.getStringExtra("post");
        final int post = Integer.parseInt(position);
        final Context ctx = this;

        recView = findViewById(R.id.imageRecycler);
        comView = findViewById(R.id.commentRecycler);
        addbtn = findViewById(R.id.addComment);
        commentDesc = findViewById(R.id.commentDesc);

        heartLike = findViewById(R.id.heartLike);
        heartDislike = findViewById(R.id.heartDislike);
        chapterLike = findViewById(R.id.likeCountChapter);
        chapterDislike = findViewById(R.id.dislikeCountChapter);
        nextBtn = findViewById(R.id.nextBtn);

//        final int[] totalLike = {0};
//        final int[] totalDislike = {0};

        myDatabase = FirebaseDatabase.getInstance().getReference();



        myDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> list = new ArrayList<>();

                for(DataSnapshot data : snapshot.child("comics").child(id).child("chapters").child(chapterId).getChildren()){
                    if(data.hasChild("image")){
                        list.add(data.child("image").getValue().toString());
                    }
                }

                final int[] totLike = {0};
                final int[] totDislike = {0};

                if(snapshot.child("comics").child(id).child("chapters").child(chapterId).hasChild("like")){
                    for(DataSnapshot like : snapshot.child("comics").child(id).child("chapters").child(chapterId).child("like").getChildren()){
                        if(like.getValue().toString().equals("true")){
                            totLike[0]++;
                        }else{
                            totDislike[0]++;
                        }
                    }
                }

                chapterLike.setText(String.valueOf(totLike[0]));
                chapterDislike.setText(String.valueOf(totDislike[0]));

//                totalDislike[0] = totDislike;
//                totalLike[0] = totLike;

                heartLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                heartDislike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });



                ComicAdapter comAdapter = new ComicAdapter(ctx,list,id,chapterId);
                recView.setAdapter(comAdapter);
                recView.setLayoutManager(new LinearLayoutManager(ctx));
                recView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final ArrayList<Comment> comList = new ArrayList<>();

        myDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comList.clear();

                for(DataSnapshot data : snapshot.child("comics").child(id).child("chapters").child(chapterId).child("comment").getChildren()){
                    Comment com = new Comment();
                    com.setName(data.child("name").getValue().toString());
                    com.setDesc(data.child("desc").getValue().toString());

                    int totLike = 0;
                    int totDislike = 0;
                    if(data.hasChild("like")){
                        for(DataSnapshot like : data.child("like").getChildren()){
                            if(like.getValue().toString().equals("true")) totLike++;
                            else totDislike++;
//                            Toast.makeText(ctx, like.getValue().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }


                    com.setLike(totLike);
                    com.setDislike(totDislike);

                    if(data.hasChild("reply")){
                        for(DataSnapshot reply : data.child("reply").getChildren()){
                            ReplyComment rep = new ReplyComment();
                            rep.setDesc(reply.child("desc").getValue().toString());
                            rep.setName(reply.child("name").getValue().toString());
                            com.addRep(rep);
                        }
                    }

                    com.setId(data.getKey());


                    comList.add(com);
                }



                if(comList.size() > 5){
                    comView.getLayoutParams().height = 1400;
                }


                CommentAdapter commentAdapter = new CommentAdapter(ctx, comList, chapterId, id);
                comView.setAdapter(commentAdapter);
                comView.setLayoutManager(new LinearLayoutManager(ctx));



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(commentDesc.getText().length() == 0){
                    Toast.makeText(ctx, "Comment can't be empty", Toast.LENGTH_SHORT).show();
                }else{
                    addCommentDatabase = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                            .child(chapterId).child("comment").push();
                    String comDesc = commentDesc.getText().toString();
                    addCommentDatabase.child("name").setValue(ProfileFragment.currentUserSession.getUsername());
                    addCommentDatabase.child("desc").setValue(comDesc);
                    Toast.makeText(ctx, "Add comment success", Toast.LENGTH_SHORT).show();

//                    Comment comAdd = new Comment();
//                    comAdd.setName(ProfileFragment.currentUserSession.getUsername());
//                    comAdd.setDesc(comDesc);
//                    comList.add(comAdd);
//
//                    CommentAdapter commentAdapter = new CommentAdapter(ctx, comList, chapterId, id);
//                    comView.setAdapter(commentAdapter);

                    commentDesc.setText("");
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(post+1 > ComicChapterActivity.comChaps.size()-1){
                    Toast.makeText(ctx, "This is the last chapter", Toast.LENGTH_SHORT).show();
                }else{
//                    Toast.makeText(ctx, "TAdas", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(v.getContext(), ReadComicActivity.class);
                    intent.putExtra("comicId", id);
                    intent.putExtra("post", String.valueOf(post+1));
                    intent.putExtra("position", ComicChapterActivity.comChaps.get(post+1).getId());
                    finish();

                    v.getContext().startActivity(intent);
                }
            }
        });



    }



    public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.MyViewHolder>{

        ArrayList<String> images;
        Context ctx;

        String comicId, chapterId;

        public ComicAdapter(Context ctx,ArrayList<String> images, String comicId, String chapterId){
            this.ctx = ctx;
            this.images = images;
            this.comicId = comicId;
            this.chapterId = chapterId;
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

    public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder>{

        ArrayList<Comment> comments;
        Context ctx;

        String chapterId;
        String id;

        public CommentAdapter(Context ctx,ArrayList<Comment> comments, String chapterId, String id){
            this.ctx = ctx;
            this.comments = comments;
            this.chapterId = chapterId;
            this.id= id;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View view = inflater.inflate(R.layout.comment_row, parent, false);

            return  new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            holder.username.setText(comments.get(position).getName());
            holder.desc.setText(comments.get(position).getDesc());
            String like,dislike;
            like = String.valueOf(comments.get(position).getLike());
            dislike = String.valueOf(comments.get(position).getDislike());
            holder.like.setText(like);
            holder.dislike.setText(dislike);
            String repCount = String.valueOf(comments.get(position).getRepCom().size());
            holder.repCount.setText("("+repCount+")");

            dbCheck = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                    .child(chapterId).child("comment").child(comments.get(position).getId());

            dbCheck.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild("like")){
                        if(snapshot.child("like").hasChild(ProfileFragment.currentUserSession.getUserId())){
                            String value = snapshot.child("like").child(ProfileFragment.currentUserSession.getUserId()).getValue().toString();
                            if(value.equals("true")){
                                holder.likeBtn.setImageResource(R.drawable.greenthumb);
                            }else{
                                holder.dislikeBtn.setImageResource(R.drawable.redthumb);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.showReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.replyComment.getVisibility() == View.VISIBLE){
                        holder.showReply.setText("Reply");
                        holder.replyComment.setVisibility(View.GONE);
                        holder.addReply.setVisibility(View.GONE);
                        holder.repCommentRec.setVisibility(View.GONE);
                    }else{
                        holder.showReply.setText("Hide Reply");
                        holder.replyComment.setVisibility(View.VISIBLE);
                        holder.addReply.setVisibility(View.VISIBLE);
                        holder.repCommentRec.setVisibility(View.VISIBLE);
                    }

                }
            });

            holder.addReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.replyComment.getText().length() == 0){
                        Toast.makeText(ctx, "Reply comment can't be empty", Toast.LENGTH_SHORT).show();
                    }else{
                        addCommentDatabase = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                                .child(chapterId).child("comment").child(comments.get(position).getId()).child("reply").push();
                        String comDesc = holder.replyComment.getText().toString();
                        addCommentDatabase.child("name").setValue(ProfileFragment.currentUserSession.getUsername());
                        addCommentDatabase.child("desc").setValue(comDesc);
                        Toast.makeText(ctx, "Add reply comment success", Toast.LENGTH_SHORT).show();

                        ReplyComment repComAdd = new ReplyComment();
                        repComAdd.setDesc(comDesc);
                        repComAdd.setName(ProfileFragment.currentUserSession.getUsername());
                        comments.get(position).addRep(repComAdd);

                        ReplyCommentAdapter repCommentAdapter = new ReplyCommentAdapter(ctx, comments.get(position).getRepCom());
                        holder.repCommentRec.setAdapter(repCommentAdapter);

                        String repCount = String.valueOf(comments.get(position).getRepCom().size());
                        holder.repCount.setText("("+repCount+")");

                        holder.replyComment.setText("");
                    }
                }
            });

            ReplyCommentAdapter repCommentAdapter = new ReplyCommentAdapter(ctx, comments.get(position).getRepCom());
            holder.repCommentRec.setAdapter(repCommentAdapter);
            holder.repCommentRec.setLayoutManager(new LinearLayoutManager(ctx));

            holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dbCheck = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                            .child(chapterId).child("comment").child(comments.get(position).getId());

                    dbCheck.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild("like")){
                                if(snapshot.child("like").hasChild(ProfileFragment.currentUserSession.getUserId())){
                                   String value = snapshot.child("like").child(ProfileFragment.currentUserSession.getUserId())
                                           .getValue().toString();
                                   if(value.equals("false")){
                                       comments.get(position).setDislike(comments.get(position).getDislike() - 1);
                                       comments.get(position).setLike(comments.get(position).getLike() + 1);
                                       addCommentDatabase = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                                               .child(chapterId).child("comment").child(comments.get(position).getId()).child("like")
                                               .child(ProfileFragment.currentUserSession.getUserId());
                                       addCommentDatabase.setValue("true");

                                       holder.likeBtn.setImageResource(R.drawable.greenthumb);
                                       holder.dislikeBtn.setImageResource(R.drawable.thumbdown);
                                   }else if(value.equals("true")){
                                       addCommentDatabase = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                                               .child(chapterId).child("comment").child(comments.get(position).getId()).child("like")
                                               .child(ProfileFragment.currentUserSession.getUserId());
                                       addCommentDatabase.removeValue();
                                       comments.get(position).setLike(comments.get(position).getLike() - 1);

                                       holder.likeBtn.setImageResource(R.drawable.thumbup);
                                   }
                                }
                            }else{
                                comments.get(position).setLike(comments.get(position).getLike() + 1);
                                addCommentDatabase = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                                        .child(chapterId).child("comment").child(comments.get(position).getId()).child("like")
                                        .child(ProfileFragment.currentUserSession.getUserId());
                                addCommentDatabase.setValue("true");

                                holder.likeBtn.setImageResource(R.drawable.greenthumb);
                            }

                            String like = String.valueOf(comments.get(position).getLike());
                            String dislike = String.valueOf(comments.get(position).getDislike());
                            holder.like.setText(like);
                            holder.dislike.setText(dislike);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            holder.dislikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    dbCheck = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                            .child(chapterId).child("comment").child(comments.get(position).getId());

                    dbCheck.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild("like")){
                                if(snapshot.child("like").hasChild(ProfileFragment.currentUserSession.getUserId())){
                                    String value = snapshot.child("like").child(ProfileFragment.currentUserSession.getUserId())
                                            .getValue().toString();
                                    if(value.equals("true")) {
                                        comments.get(position).setLike(comments.get(position).getLike() - 1);
                                        comments.get(position).setDislike(comments.get(position).getDislike() + 1);

                                        addCommentDatabase = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                                                .child(chapterId).child("comment").child(comments.get(position).getId()).child("like")
                                                .child(ProfileFragment.currentUserSession.getUserId());
                                        addCommentDatabase.setValue("false");

                                        holder.dislikeBtn.setImageResource(R.drawable.redthumb);
                                        holder.likeBtn.setImageResource(R.drawable.thumbup);

                                    }else if(value.equals("false")){
                                        addCommentDatabase = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                                                .child(chapterId).child("comment").child(comments.get(position).getId()).child("like")
                                                .child(ProfileFragment.currentUserSession.getUserId());
                                        addCommentDatabase.removeValue();
                                        comments.get(position).setDislike(comments.get(position).getDislike() - 1);

                                        holder.dislikeBtn.setImageResource(R.drawable.thumbdown);

                                    }
                                }
                            }else{
                                comments.get(position).setDislike(comments.get(position).getDislike() + 1);
                                addCommentDatabase = FirebaseDatabase.getInstance().getReference().child("comics").child(id).child("chapters")
                                        .child(chapterId).child("comment").child(comments.get(position).getId()).child("like")
                                        .child(ProfileFragment.currentUserSession.getUserId());
                                addCommentDatabase.setValue("false");

                                holder.dislikeBtn.setImageResource(R.drawable.redthumb);
                            }

                            String like = String.valueOf(comments.get(position).getLike());
                            String dislike = String.valueOf(comments.get(position).getDislike());
                            holder.like.setText(like);
                            holder.dislike.setText(dislike);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView username, desc, like, dislike, showReply,repCount;
            EditText replyComment;
            Button addReply;
            RecyclerView repCommentRec;
            ImageView likeBtn, dislikeBtn;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.usernameComment);
                desc = itemView.findViewById(R.id.commentDesc);
                like = itemView.findViewById(R.id.likeCount);
                dislike = itemView.findViewById(R.id.notLikeCount);
                replyComment = itemView.findViewById(R.id.replyCommentDesc);
                addReply = itemView.findViewById(R.id.addReplyComment);
                showReply = itemView.findViewById(R.id.showReply);
                repCount = itemView.findViewById(R.id.replyCount);
                repCommentRec = itemView.findViewById(R.id.repCommentRec);
                likeBtn = itemView.findViewById(R.id.likeBtn);
                dislikeBtn = itemView.findViewById(R.id.dislikeBtn);
            }
        }
    }

    public class ReplyCommentAdapter extends RecyclerView.Adapter<ReplyCommentAdapter.MyViewHolder>{

        ArrayList<ReplyComment> repComments;
        Context ctx;


        public ReplyCommentAdapter(Context ctx,ArrayList<ReplyComment> repComments){
            this.ctx = ctx;
            this.repComments = repComments;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View view = inflater.inflate(R.layout.reply_comment_row, parent, false);

            return  new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            holder.repUsername.setText(repComments.get(position).getName());
            holder.repDesc.setText(repComments.get(position).getDesc());

        }

        @Override
        public int getItemCount() {
            return repComments.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView repUsername, repDesc;
            View garisRep;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                repUsername = itemView.findViewById(R.id.repUsernameComment);
                repDesc = itemView.findViewById(R.id.repCommentDesc);
                garisRep = itemView.findViewById(R.id.garisRep);

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