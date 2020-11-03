package com.example.tpa_android_decomics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class PremiumFragment extends Fragment {

    TextView notPremTxt;
    TextView premTxt;
    ImageView imageGift, premGift;
    Button btn;
    FirebaseAuth auth;
    FirebaseUser user;



    private PremiumFragment() {
        // Required empty public constructor
    }

    private static PremiumFragment instance = null;

    public static PremiumFragment getInstance(){
        if(instance == null){
            instance = new PremiumFragment();
        }
        return instance;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_premium, container, false);



        premTxt = view.findViewById(R.id.premiumText);
        notPremTxt = view.findViewById(R.id.notPremiumText);
        btn = view.findViewById(R.id.buyBtn);
        imageGift = view.findViewById(R.id.imageBuy);
        premGift = view.findViewById(R.id.premiumGift);

        auth = FirebaseAuth.getInstance();
         user = auth.getCurrentUser();

        if(!user.isEmailVerified()){
            premTxt.setVisibility(View.GONE);
            premGift.setVisibility(View.GONE);
        }else{
            notPremTxt.setVisibility(View.GONE);
            btn.setVisibility(View.GONE);
            imageGift.setVisibility(View.GONE);

        }
    

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(view.getContext(), getResources().getString(R.string.buya), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });



        return view;
    }
}