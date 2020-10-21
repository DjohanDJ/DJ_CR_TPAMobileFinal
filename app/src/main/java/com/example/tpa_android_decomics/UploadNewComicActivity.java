package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
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

import com.example.tpa_android_decomics.models.Comic;
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

public class UploadNewComicActivity extends AppCompatActivity {

    private EditText comicName, comicDesc;
    private Button chooseFileBtn, submitButton;
    private Uri mImageUri;
    private ImageView mImageView;
    private Spinner dropdownGenre;
    private String genre = "Romance";
    private static final int PICK_IMAGE_REQUEST = 1;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_new_comic);

        initializeAttribute();


        chooseFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
                genre = dropdownGenre.getItemAtPosition((int) dropdownGenre.getSelectedItemId()).toString();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(UploadNewComicActivity.this, getResources().getString(R.string.uploadStillPro), Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
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
        if (comicName.getText().toString().isEmpty()) {
            Toast.makeText(UploadNewComicActivity.this, getResources().getString(R.string.comicNameEmp), Toast.LENGTH_SHORT).show();
        } else if (comicDesc.getText().toString().length() < 20) {
            Toast.makeText(UploadNewComicActivity.this, getResources().getString(R.string.synopsis), Toast.LENGTH_SHORT).show();
        } else if (mImageUri == null) {
            Toast.makeText(UploadNewComicActivity.this, getResources().getString(R.string.thumbnail), Toast.LENGTH_SHORT).show();
        } else {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(UploadNewComicActivity.this, getResources().getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            UploadComic newComic = new UploadComic();
                            newComic.setName(comicName.getText().toString());
                            newComic.setDescription(comicDesc.getText().toString());
                            newComic.setGenre(genre);
                            newComic.setImage(uri.toString());
                            newComic.setPremium("false");
                            newComic.setUserId(ProfileFragment.currentUserSession.getUserId());
                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(newComic);
                            startActivity(new Intent(UploadNewComicActivity.this, BottomNavigationActivity.class));
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadNewComicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                }
            });
        }
    }

    private void initializeAttribute() {
        this.dropdownGenre = findViewById(R.id.dropdownGenreId);
        this.dropdownGenre.setAdapter(new ArrayAdapter<>(UploadNewComicActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.genre_comic)));
//        ((TextView) (this.dropdownGenre.getItemAtPosition(1))).setTextColor(Color.BLUE);
        this.chooseFileBtn = findViewById(R.id.chooseFileId);
        this.mImageView = findViewById(R.id.mainImageId);
        this.comicName = findViewById(R.id.comicNameId);
        this.comicDesc = findViewById(R.id.comicDescription);
        this.submitButton = findViewById(R.id.submitBtn);
        mStorageRef = FirebaseStorage.getInstance().getReference("comicimages");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("comics");
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
        }

    }
}