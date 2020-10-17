package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.UploadChapter;
import com.example.tpa_android_decomics.models.UploadComic;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class UploadChapterActivity extends AppCompatActivity {

    private EditText chapterName;
    private TextView uploadedCounter;
    private Button chooseFileBtn, commitButton, finishButton;
    private Uri mImageUri;
    private ImageView mImageView;
    private static final int PICK_IMAGE_REQUEST = 1;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private String comicSelectedId;
    private String finalMessage;
    private Integer countUpload = 0;

    private StorageTask mUploadTask;
    private boolean imageHolder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_chapter);

        Intent intent = getIntent();
        comicSelectedId = intent.getStringExtra("asdf");

        initializeAttribute();


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
                            mDatabaseRef.child(uploadId).setValue(newChapter);
                            mImageView.setImageResource(R.drawable.shape);
                            imageHolder = false;
                            countUpload++;
                            uploadedCounter.setText(finalMessage + " " + countUpload.toString());
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
    }
}