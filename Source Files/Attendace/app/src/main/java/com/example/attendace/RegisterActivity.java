package com.example.attendace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextName, editTextRollNo, editTextPassword;
    private Button buttonRegister;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private EditText registrationNumber, mobileNumber;
    private ImageView imageViewProfilePhoto;
    private Uri profilePhotoUri;
    private static final int PICK_IMAGE = 1;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("profile_images");

        editTextEmail = findViewById(R.id.email);
        editTextName = findViewById(R.id.editTextName);
        editTextRollNo = findViewById(R.id.editTextNumber);
        registrationNumber = findViewById(R.id.registrationNumber);
        editTextPassword = findViewById(R.id.password);
        mobileNumber = findViewById(R.id.mobileNumber);
        buttonRegister = findViewById(R.id.register);
        imageViewProfilePhoto = findViewById(R.id.imageViewProfilePhoto);

        imageViewProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickProfilePhoto();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerStudent();
            }
        });
    }

    private void pickProfilePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profilePhotoUri = data.getData();
            Glide.with(this)
                    .load(profilePhotoUri)
                    .apply(RequestOptions.circleCropTransform()) // Apply circular cropping
                    .into(imageViewProfilePhoto);
        }
    }

    private void registerStudent() {
        final String email = editTextEmail.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String rollNo = editTextRollNo.getText().toString().trim();
        final String mobileno = mobileNumber.getText().toString().trim();
        final String regno = registrationNumber.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(rollNo) || TextUtils.isEmpty(password) || TextUtils.isEmpty(regno) || TextUtils.isEmpty(mobileno)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profilePhotoUri == null) {
            Toast.makeText(this, "Please select a profile photo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Change the reference to the "students" node
        databaseReference.child("students").child(rollNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(RegisterActivity.this, "RollNo already registered. Please use a different RollNo.", Toast.LENGTH_SHORT).show();
                } else {
                    // Use the FirebaseAuth to create the user
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // If user creation is successful, upload the profile photo and student data
                                        uploadProfilePhoto(rollNo, name, email, mobileno, regno);
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RegisterActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void uploadProfilePhoto(final String rollNo, final String name, final String email, final String mobileno, final String regno) {
        Log.d("MyApp", "Starting profile photo upload for " + rollNo);
        StorageReference profilePhotoRef = storageReference.child("Students").child(rollNo + "_profile.jpg");

        UploadTask uploadTask = profilePhotoRef.putFile(profilePhotoUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads with a specific message or dialog.
                final String errorMessage = "Failed to upload profile photo: " + e.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                profilePhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        DatabaseReference currentUserDb = databaseReference.child("students").child(rollNo);
                        currentUserDb.child("name").setValue(name);
                        currentUserDb.child("email").setValue(email);
                        currentUserDb.child("mobileno").setValue(mobileno);
                        currentUserDb.child("regno").setValue(regno);
                        currentUserDb.child("profilePhotoUrl").setValue(downloadUri.toString());
                        currentUserDb.child("approvalStatus").setValue("pending");
                        currentUserDb.child("profilePhotoUrl").setValue(downloadUri.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        final String errorMessage = "Failed to get profile photo URL: " + e.getMessage();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }
}