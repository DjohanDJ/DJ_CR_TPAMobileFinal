package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tpa_android_decomics.models.LoadingAnimation;
import com.example.tpa_android_decomics.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailId, passwordId;
    private Button signInButton, signUpButton;
    private SignInButton signInGoogleButton;
    private FirebaseAuth myFireBaseAuth;
    private FirebaseAuth.AuthStateListener myAuthListener;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 1;
    private DatabaseReference myRealDatabase;
    private LoadingAnimation loadingAnimation;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        doInitializeTemplates();
    }

    private void doInitializeTemplates() {
        emailId = findViewById(R.id.emailId);
        passwordId = findViewById(R.id.passwordId);
        signInButton = findViewById(R.id.signInButtonId);
        signUpButton = findViewById(R.id.signUpButtonId);
        myFireBaseAuth = FirebaseAuth.getInstance();
        myRealDatabase = FirebaseDatabase.getInstance().getReference();
        myAuthListener = new FirebaseAuth.AuthStateListener() {
            FirebaseUser myFirebaseUser = myFireBaseAuth.getCurrentUser();
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (myFirebaseUser != null) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.sign_in_message), Toast.LENGTH_SHORT).show();
                }
            }
        };
        loadingAnimation = new LoadingAnimation(LoginActivity.this);
        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        final String currentUserId = sharedPreferences.getString("user_userId", "");
        if (!currentUserId.equals("")) {
            myRealDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = new User();
                    user.setUserId(currentUserId);
                    user.setUsername(snapshot.child("username").getValue().toString());
                    ProfileFragment.currentUserSession = user;
                    Intent intent = new Intent(LoginActivity.this, BottomNavigationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        doCheckSignInButton();
        doCheckSignUpButton();
        createRequest();
        doCheckSignInGoogleButton();
    }

    private void doCheckSignInGoogleButton() {
        signInGoogleButton = findViewById(R.id.signInGoogleButtonId);
        signInGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void createRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.sign_google_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        myFireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = myFireBaseAuth.getCurrentUser();
                            String userId = task.getResult().getUser().getUid();
                            myRealDatabase.child("users").child(userId).child("username").setValue(user.getDisplayName());
                            User newUser = new User();
                            newUser.setUsername(user.getDisplayName());
                            newUser.setUserId(userId);
                            ProfileFragment.currentUserSession = newUser;
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_userId", userId);
                            editor.apply();
                            loadingAnimation.startLoading();
                            startActivity(new Intent(LoginActivity.this, BottomNavigationActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.sign_google_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void doCheckSignUpButton() {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void doCheckSignInButton() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email, password;
                email = emailId.getText().toString();
                password = passwordId.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.email_error), Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.pass_error), Toast.LENGTH_SHORT).show();
                } else {
                    emailId.setKeyListener(null);
                    passwordId.setKeyListener(null);
                    myFireBaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show();
                                emailId.setKeyListener(new EditText(getApplicationContext()).getKeyListener());
                                passwordId.setKeyListener(new EditText(getApplicationContext()).getKeyListener());
                            } else {
                                // TODO Pas login disini, harus invite.
                                final String userId = task.getResult().getUser().getUid();
//                                Toast.makeText(LoginActivity.this, userId, Toast.LENGTH_SHORT).show();
                                myRealDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                                            Toast.makeText(LoginActivity.this, dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                                            if (dataSnapshot.getKey().equals(userId)) {
                                                User user = new User();
                                                user.setUserId(userId);
                                                user.setUsername(dataSnapshot.child("username").getValue().toString());
                                                ProfileFragment.currentUserSession = user;
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("user_userId", userId);
                                                editor.apply();
                                                startActivity(new Intent(LoginActivity.this, BottomNavigationActivity.class));
                                                loadingAnimation.startLoading();
                                                finish();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}