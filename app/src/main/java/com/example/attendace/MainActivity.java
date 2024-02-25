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

public class MainActivity extends AppCompatActivity {
    private EditText textEmailAddress, textPassword;
    private CheckBox showPasswordCheckBox;
    private Button openintern, forgotpassword;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "login_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        textEmailAddress = findViewById(R.id.Email);
        textPassword = findViewById(R.id.Password);
        openintern = findViewById(R.id.Signin);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);

        showPasswordCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = showPasswordCheckBox.isChecked();

                if (isChecked) {
                    textPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    textPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        openintern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInStudent();
            }
        });

        if (isLoggedIn()) {
            navigateToInternalActivity();
        }

    }

    private void saveLoginStatus(boolean isLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void openRegister(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void Goadmin(View view) {
        startActivity(new Intent(this, TeacherView.class));
    }

    public void forgotpassword(View view) {
        Intent intent = new Intent(MainActivity.this, ForgotPassword.class);
        intent.putExtra("userType", "student");
        startActivity(intent);
    }

    private void navigateToInternalActivity() {
        startActivity(new Intent(MainActivity.this, Internal.class));
        finish();
    }

    private void signInStudent() {
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
                            saveLoginStatus(false);
                            Toast.makeText(MainActivity.this, "Sign-in failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                            Log.e("SignIn", "Sign-in failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void checkUserDetails(final String email) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("students");
        Query query = userRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String approvalStatus = child.child("approvalStatus").getValue(String.class);

                        Log.d("CheckUser", "ApprovalStatus: " + approvalStatus);

                        if ("approved".equals(approvalStatus)) {
                            saveLoginStatus(true);
                            navigateToInternalActivity();
                        } else if ("pending".equals(approvalStatus)) {
                            Toast.makeText(MainActivity.this, "Your student account is not approved yet.", Toast.LENGTH_SHORT).show();
                        } else if ("rejected".equals(approvalStatus)) {
                            saveLoginStatus(false);
                            Toast.makeText(MainActivity.this, "Your student account is rejected.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Unknown approval status for student.", Toast.LENGTH_SHORT).show();
                        }
                        return; // Stop processing once user details are checked
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User data not found for email: " + email, Toast.LENGTH_SHORT).show();
                    Log.e("CheckUser", "User data not found for email: " + email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CheckUser", "Database error: " + databaseError.getMessage());
            }
        });
    }
}