package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameId, emailId, passwordId, rePasswordId;
    private Button signUpButton;
    private FirebaseAuth myFireBaseAuth;
    private DatabaseReference myRealDatabase;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        doInitializeTemplates();
    }

    private void doInitializeTemplates() {
        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        usernameId = findViewById(R.id.usernameId);
        emailId = findViewById(R.id.emailId);
        passwordId = findViewById(R.id.passwordId);
        rePasswordId = findViewById(R.id.rePasswordId);
        signUpButton = findViewById(R.id.signUpButtonId);
        myFireBaseAuth = FirebaseAuth.getInstance();
        myRealDatabase = FirebaseDatabase.getInstance().getReference();
        doCheckSignUpButton();
    }

    private void doCheckSignUpButton() {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username, email, password, rePassword;
                username = usernameId.getText().toString();
                email = emailId.getText().toString();
                password = passwordId.getText().toString();
                rePassword = rePasswordId.getText().toString();

                if (username.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.username_validation), Toast.LENGTH_SHORT).show();
                } else if (!validEmail(email)) {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.userEmail_validation), Toast.LENGTH_SHORT).show();
                } else if (password.length() < 8) {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.userPass_validation), Toast.LENGTH_SHORT).show();
                } else if (!rePassword.equals(password)) {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.userRePass_validation), Toast.LENGTH_SHORT).show();
                } else {
                    myFireBaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.sign_up_error), Toast.LENGTH_SHORT).show();
                            } else {
                                String userId = task.getResult().getUser().getUid();
                                myRealDatabase.child("users").child(userId).child("username").setValue(username);
                                User newUser = new User();
                                newUser.setUsername(username);
                                newUser.setUserId(userId);
                                ProfileFragment.currentUserSession = newUser;
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("user_userId", userId);
                                editor.apply();
                                finish();
                                startActivity(new Intent(RegisterActivity.this, BottomNavigationActivity.class));

                            }
                        }
                    });
                }
            }
        });
    }

    private boolean validEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

}