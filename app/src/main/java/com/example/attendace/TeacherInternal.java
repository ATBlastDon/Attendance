package com.example.attendace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherInternal extends AppCompatActivity {

    private TextView studentNameTextView;
    private Button approveButton;
    private Button rejectButton;
    private DatabaseReference databaseReference;

    private List<String> pendingStudentRollNumbers;
    private int currentStudentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_internal);

        studentNameTextView = findViewById(R.id.studentNameTextView);
        approveButton = findViewById(R.id.approveButton);
        rejectButton = findViewById(R.id.rejectButton);

        // Initialize the database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("students");

        pendingStudentRollNumbers = new ArrayList<>();
        currentStudentIndex = 0;

        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveStudent();
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectStudent();
            }
        });
        fetchPendingStudents();
    }

    private void fetchPendingStudents() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingStudentRollNumbers.clear(); // Clear the list

                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    String approvalStatus = studentSnapshot.child("approvalStatus").getValue(String.class);
                    if ("pending".equals(approvalStatus)) {
                        String studentRollNo = studentSnapshot.getKey(); // Retrieve Roll No
                        pendingStudentRollNumbers.add(studentRollNo);
                    }
                }
                currentStudentIndex = 0;
                displayNextPendingStudent();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherInternal.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNextPendingStudent() {
        if (currentStudentIndex < pendingStudentRollNumbers.size()) {
            String studentRollNo = pendingStudentRollNumbers.get(currentStudentIndex);

            DatabaseReference studentRef = databaseReference.child(studentRollNo);

            studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String studentName = dataSnapshot.child("name").getValue(String.class);
                        studentNameTextView.setText(studentName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(TeacherInternal.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            studentNameTextView.setText("No pending students to approve");
        }
    }


    private void rejectStudent() {
        if (currentStudentIndex < pendingStudentRollNumbers.size()) {
            String studentRollNo = pendingStudentRollNumbers.get(currentStudentIndex);

            databaseReference.child(studentRollNo).child("approvalStatus").setValue("rejected");

            Toast.makeText(this, "Student Rejected!", Toast.LENGTH_SHORT).show();

            currentStudentIndex++;
            displayNextPendingStudent();
        }
    }

    private void approveStudent() {
        if (currentStudentIndex < pendingStudentRollNumbers.size()) {
            String studentRollNo = pendingStudentRollNumbers.get(currentStudentIndex);

            databaseReference.child(studentRollNo).child("approvalStatus").setValue("approved");

            getEmailAndSendApprovalEmail(studentRollNo);

            Toast.makeText(this, "Student Approved!", Toast.LENGTH_SHORT).show();

            currentStudentIndex++;
            displayNextPendingStudent();
        }
    }

    private void getEmailAndSendApprovalEmail(String studentRollNo) {
        DatabaseReference studentRef = databaseReference.child(studentRollNo);

        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String studentEmail = dataSnapshot.child("email").getValue(String.class);
                    sendApprovalEmail(studentEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherInternal.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendApprovalEmail(String studentEmail) {
        Log.d("EmailDebug", "Preparing to send email to: " + studentEmail);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { studentEmail });
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Approval Notification");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, you have been Approved by your teacher!");

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            Log.d("EmailDebug", "Starting email client.");
            startActivity(emailIntent);
        } else {
            Log.e("EmailDebug", "No email client available.");
        }
    }


}
