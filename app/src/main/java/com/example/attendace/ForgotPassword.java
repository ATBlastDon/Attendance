package com.example.attendace;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ForgotPassword extends AppCompatActivity {

    private EditText textEmailAddress;
    private Button resetPass;
    private FirebaseAuth mAuth;
    private String userType; // Added variable to store user type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        textEmailAddress = findViewById(R.id.forgotemail);
        resetPass = findViewById(R.id.Reset);
        mAuth = FirebaseAuth.getInstance();

        // Retrieve user type from the intent
        userType = getIntent().getStringExtra("userType");

        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = textEmailAddress.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    textEmailAddress.setError("Please enter your email");
                    return;
                }

                // Check user type and send password reset email accordingly
                if ("student".equals(userType)) {
                    sendPasswordResetEmailForStudent(email);
                } else if ("teacher".equals(userType)) {
                    sendPasswordResetEmailForTeacher(email);
                }
            }
        });
    }

    private void sendPasswordResetEmailForStudent(String email) {
        // Add logic to check if the email belongs to a student
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference().child("Users").child("students");
        Query query = studentRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email belongs to a student, send password reset email
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ForgotPassword.this, "Password reset email sent. Please check your inbox.",
                                                Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ForgotPassword.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(ForgotPassword.this, "Password reset email could not be sent. Please try again.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    // Email does not belong to a student
                    Toast.makeText(ForgotPassword.this, "Invalid email. Please enter a student email.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ForgotPassword.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendPasswordResetEmailForTeacher(String email) {
        // Add logic to check if the email belongs to a teacher
        DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference().child("Users").child("teachers");
        Query query = teacherRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email belongs to a teacher, send password reset email
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ForgotPassword.this, "Password reset email sent. Please check your inbox.",
                                                Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ForgotPassword.this, TeacherView.class));
                                        finish();
                                    } else {
                                        Toast.makeText(ForgotPassword.this, "Password reset email could not be sent. Please try again.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    // Email does not belong to a teacher
                    Toast.makeText(ForgotPassword.this, "Invalid email. Please enter a teacher email.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ForgotPassword.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
