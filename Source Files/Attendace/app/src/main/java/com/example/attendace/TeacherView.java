package com.example.attendace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class TeacherView extends AppCompatActivity {

    private EditText textEmailAddress, textPassword;
    private Button LoginAdmin, reglogin, adminForpassword;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "login_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private CheckBox showPasswordCheckBox;

    private Button Gostudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_view);

        textEmailAddress = findViewById(R.id.TeacherEmail);
        textPassword = findViewById(R.id.TeacherPassword);
        LoginAdmin = findViewById(R.id.LoginAdmin);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);
        reglogin = findViewById(R.id.regAdmin);
        adminForpassword = findViewById(R.id.adminFP);
        firebaseAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);


        showPasswordCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check the CheckBox state
                boolean isChecked = showPasswordCheckBox.isChecked();

                // Update the password EditText accordingly
                if (isChecked) {
                    // Show the password text
                    textPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // Hide the password text
                    textPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        LoginAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInTeacher();
            }
        });

        if (isLoggedIn()) {
            navigateToTeacherInternalActivity();
        }
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void adminFp(View view) {
        Intent intent = new Intent(TeacherView.this, ForgotPassword.class);
        intent.putExtra("userType", "teacher");
        startActivity(intent);
    }

    private void navigateToTeacherInternalActivity() {
        startActivity(new Intent(TeacherView.this, TeacherHome.class));
        finish();
    }

    public void Gostudent(View view) {
        startActivities(new Intent[]{new Intent(this, MainActivity.class)});
    }

    public void adminlogin(View view) {
        startActivities(new Intent[]{new Intent(this, MainActivity.class)});
    }

    public void reglogin(View view) {
        startActivities(new Intent[]{new Intent(this, Teacher_Register.class)});
    }

    private void signInTeacher() {
        final String email = textEmailAddress.getText().toString().trim();
        String password = textPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign in the user with Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("SignIn", "Sign-in successful");
                            checkUserDetails(email);
                        } else {
                            Toast.makeText(TeacherView.this, "Sign-in failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                            Log.e("SignIn", "Sign-in failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void checkUserDetails(final String email) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("teachers");
        Query query = userRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User found, navigate to the teacher's internal activity
                    navigateToTeacherInternalActivity();
                } else {
                    // User not found
                    Toast.makeText(TeacherView.this, "Teacher data not found for email: " + email, Toast.LENGTH_SHORT).show();
                    Log.e("CheckUser", "Teacher data not found for email: " + email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherView.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CheckUser", "Database error: " + databaseError.getMessage());
            }
        });
    }

}