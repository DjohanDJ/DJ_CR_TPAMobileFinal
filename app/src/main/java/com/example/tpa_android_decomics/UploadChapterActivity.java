package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.TempImage;
import com.example.tpa_android_decomics.models.UploadChapter;
import com.example.tpa_android_decomics.models.UploadComic;
import com.example.tpa_android_decomics.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UploadChapterActivity extends AppCompatActivity {

    private EditText chapterName;
    private TextView uploadedCounter;
    private Button chooseFileBtn, commitButton, finishButton;
    private Uri mImageUri;
    private ImageView mImageView;
    private static final int PICK_IMAGE_REQUEST = 1;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference dbNotif;
    private String comicSelectedId;
    private String finalMessage;
    private Integer countUpload = 0;

    private StorageTask mUploadTask;
    private boolean imageHolder = false;

    private RecyclerView myImageRecycler;
    private DatabaseReference myDatabase;
    private ArrayList<TempImage> tempImages;
    final Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_chapter);

        createNotifcation();

        Intent intent = getIntent();
        comicSelectedId = intent.getStringExtra("asdf");

        initializeAttribute();
        buttonChecker();

//        myDatabase = FirebaseDatabase.getInstance().getReference();
//        myDatabase.child("comics").child(comicSelectedId).child("chapters").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                tempImages.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    TempImage tempImage = new TempImage();
//                    tempImage.setImagePath(dataSnapshot.child("image").getValue().toString());
//                    tempImages.add(tempImage);
//                }
//
//                UploadChapterActivity.ImageAdapter imageAdapter = new UploadChapterActivity.ImageAdapter(ctx, tempImages);
//                myImageRecycler.setAdapter(imageAdapter);
//                myImageRecycler.setLayoutManager(new LinearLayoutManager(ctx));
////                recView.setLayoutManager(new GridLayoutManager(view.getContext(),3));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    void createNotifcation(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    "channel1",
                    "Channel 1",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel1.setDescription("This is channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }


    private void buttonChecker() {
        chooseFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(UploadChapterActivity.this, getResources().getString(R.string.uploadStillPro), Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();

                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(UploadChapterActivity.this, getResources().getString(R.string.uploadStillPro), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(UploadChapterActivity.this, ComicChapterActivity.class);
                    intent.putExtra("asd", comicSelectedId);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadFile() {
        if (chapterName.getText().toString().isEmpty()) {
            Toast.makeText(UploadChapterActivity.this, getResources().getString(R.string.chapterNameEmp), Toast.LENGTH_SHORT).show();
        } else if (mImageUri == null || imageHolder == false) {
            Toast.makeText(UploadChapterActivity.this, getResources().getString(R.string.chapterImg), Toast.LENGTH_SHORT).show();
        } else {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(UploadChapterActivity.this, getResources().getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(Uri uri) {
                            UploadChapter newChapter = new UploadChapter();
                            newChapter.setName(chapterName.getText().toString());
                            newChapter.setImage(uri.toString());
                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(newChapter.getName()).child(uploadId).setValue(newChapter);
                            mImageView.setImageResource(R.drawable.shape);
                            imageHolder = false;
                            countUpload++;
                            uploadedCounter.setText(finalMessage + " " + countUpload.toString());
                            TempImage tempImage = new TempImage();
                            tempImage.setImagePath(uri.toString());
                            tempImages.add(tempImage);
                            ImageAdapter imageAdapter = new ImageAdapter(ctx, tempImages);
                            myImageRecycler.setAdapter(imageAdapter);
                            myImageRecycler.setLayoutManager(new LinearLayoutManager(ctx));

                            pushNotif(newChapter, comicSelectedId);

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                }
            });
        }
    }

    void pushNotif(final UploadChapter newChapter, String id){

        final Context ctx = this;


        dbNotif = FirebaseDatabase.getInstance().getReference("comics").child(comicSelectedId);

        final String[] comicName = {""};

        dbNotif.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comicName[0] = snapshot.child("name").getValue().toString();

                int total = 0;

                for(DataSnapshot chap : snapshot.child("chapters").child(newChapter.getName()).getChildren()){
                    total++;
                }

                if(total == 1){
                    Notification builder = new NotificationCompat.Builder(
                            UploadChapterActivity.this, "channel1"
                    )
                            .setSmallIcon(R.mipmap.ic_banner)
                            .setContentTitle(comicName[0])
                            .setContentText(newChapter.getName() + " has been released")
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE).build();

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
                    notificationManager.notify(1,builder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
//            Picasso.with(ctx).load(mImageUri).into(mImageView);
            mImageView.setImageURI(mImageUri);
            imageHolder = true;
        }

    }

    private void initializeAttribute() {
        countUpload = 0;
        this.chooseFileBtn = findViewById(R.id.chooseFileId);
        this.mImageView = findViewById(R.id.mainImageId);
        this.chapterName = findViewById(R.id.comicNameId);
        this.commitButton = findViewById(R.id.submitBtn);
        this.uploadedCounter = findViewById(R.id.uploadedCounter);
        this.finishButton = findViewById(R.id.finishBtn);
        finalMessage = uploadedCounter.getText().toString();
        mStorageRef = FirebaseStorage.getInstance().getReference("chapterimages");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("comics").child(comicSelectedId).child("chapters");
        this.myImageRecycler = findViewById(R.id.recViewImage);
        this.tempImages = new ArrayList<>();
    }

    public class ImageAdapter extends RecyclerView.Adapter<UploadChapterActivity.ImageAdapter.MyViewHolder> {

        ArrayList<TempImage> images;
        Context ctx;

        public ImageAdapter(Context ctx, ArrayList<TempImage> images){
            this.ctx = ctx;
            this.images = images;
        }

        @NonNull
        @Override
        public UploadChapterActivity.ImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            // TODO ganti template
            View view = inflater.inflate(R.layout.card_image_row, parent, false);


            return new UploadChapterActivity.ImageAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            UploadChapterActivity.LoadImageUrl loadImage = new UploadChapterActivity.LoadImageUrl(holder.myImage);
            loadImage.execute(images.get(position).getImagePath());
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView myImage;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                myImage = itemView.findViewById(R.id.imageLoader);
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