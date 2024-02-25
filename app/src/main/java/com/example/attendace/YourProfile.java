package com.example.attendace;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class YourProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView txtName, txtrollno, txtemail ,mobileNumber ,Regno;
    private ImageView imageViewProfilePhoto;
    private TextView YourProfilecheck;
    private ProgressBar loadingIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        txtName = findViewById(R.id.txtName);
        txtrollno = findViewById(R.id.txtrollno);
        txtemail = findViewById(R.id.txtemail);
        mobileNumber = findViewById(R.id.mobileNumber);
        Regno = findViewById(R.id.Regno);
        YourProfilecheck = findViewById(R.id.YourProfilecheck);
        imageViewProfilePhoto = findViewById(R.id.imageViewProfilePhoto);

        loadingIndicator = findViewById(R.id.loadingIndicator);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            showLoadingIndicator();
            String userEmail = user.getEmail();
            checkUserInfo(userEmail);
        } else {
            YourProfilecheck.setText("Welcome, Guest");
        }
    }

    private void showLoadingIndicator() {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingIndicator.setVisibility(View.GONE);
    }

    private void checkUserInfo(String userEmail) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child("students");
        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String rollNo = childSnapshot.getKey();
                        String name = childSnapshot.child("name").getValue(String.class);
                        String email = childSnapshot.child("email").getValue(String.class);
                        String regno = childSnapshot.child("regno").getValue(String.class);
                        String mobileno = childSnapshot.child("mobileno").getValue(String.class);
                        String profilePhotoUrl = childSnapshot.child("profilePhotoUrl").getValue(String.class);

                        Log.d("YourProfile", "Name: " + name);
                        Log.d("YourProfile", "Email: " + email);
                        Log.d("YourProfile", "Reg No: " + regno);
                        Log.d("YourProfile", "Mobile Number: " + mobileno);
                        Log.d("YourProfile", "Profile Photo: " + profilePhotoUrl);

                        txtName.setText("Name:" + name);
                        txtrollno.setText("Roll No:" + rollNo);
                        txtemail.setText("Email:" + email);
                        mobileNumber.setText("Mobile Number:" + mobileno);
                        Regno.setText("Reg No:" + regno);
                        YourProfilecheck.setText(name + "  \uD83D\uDE4B\uD83C\uDFFB\u200D♂ ");

                        // Load the profile photo using Glide
                        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                            Glide.with(YourProfile.this)
                                    .load(profilePhotoUrl)
                                    .apply(RequestOptions.circleCropTransform()) // Optional: Apply circular cropping
                                    .into(imageViewProfilePhoto);
                        }
                    }
                } else {
                    YourProfilecheck.setText("Welcome, Guest");
                }hideLoadingIndicator();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(YourProfile.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
