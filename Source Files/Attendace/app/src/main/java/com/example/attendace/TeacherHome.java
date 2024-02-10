package com.example.attendace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class TeacherHome extends AppCompatActivity {

    private Button studentdb, approve, genrate, approved;
    private TextView textView11;
    private ImageView TeacherProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        studentdb = findViewById(R.id.studentdatabase);
        approve = findViewById(R.id.approval);
        genrate = findViewById(R.id.Genrate);
        approved = findViewById(R.id.approvedstudent);
        textView11 = findViewById(R.id.textView11);
        TeacherProfile = findViewById(R.id.TeacherProfile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();

            checkUserRollNumber(userEmail);
        } else {
            textView11.setText("Guest");
        }


        studentdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String spreadsheetUrl = "https://attendance-2cd1f-default-rtdb.firebaseio.com/";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(spreadsheetUrl));
                startActivity(intent);
            }
        });

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TeacherHome.this, TeacherInternal.class));
            }
        });

        genrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TeacherHome.this, GenerateQR.class));
            }
        });

        approved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TeacherHome.this, ApprovedStudents.class));
            }
        });
    }

    private void checkUserRollNumber(String userEmail) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child("teachers");
        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String profilePhotoUrl = userSnapshot.child("profilePhotoUrl").getValue(String.class);
                        String rollNo = userSnapshot.child("rollNo").getValue(String.class);
                        if (rollNo != null && !rollNo.isEmpty()) {
                            textView11.setText(userSnapshot.child("name").getValue(String.class));
                        } else {
                            textView11.setText(userSnapshot.child("name").getValue(String.class) + "  \uD83D\uDE4B\uD83C\uDFFB\u200D♂ ");
                        }
                        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                            Glide.with(TeacherHome.this)
                                    .load(profilePhotoUrl)
                                    .apply(RequestOptions.circleCropTransform()) // Optional: Apply circular cropping
                                    .into(TeacherProfile);
                        }
                    }
                } else {
                    textView11.setText("Welcome, Guest");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherHome.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
