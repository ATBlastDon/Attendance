package com.example.attendace;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ApprovedStudents extends AppCompatActivity {

    private ListView approvedStudentsListView;
    private ListView pendingStudentsListView; // Add ListView for pending students
    private ProgressBar loadingforApproved;
    private ArrayAdapter<String> approvedAdapter;
    private ArrayAdapter<String> pendingAdapter; // Add ArrayAdapter for pending students
    private ArrayList<String> approvedStudentNames;
    private ArrayList<String> pendingStudentNames; // Add ArrayList for pending students

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approved_students);

        approvedStudentsListView = findViewById(R.id.approvedStudentsListView);
        pendingStudentsListView = findViewById(R.id.pendingStudentsListView); // Initialize pending students ListView
        loadingforApproved = findViewById(R.id.loadingIndicator);

        // Initialize the adapters and lists for approved and pending students
        approvedStudentNames = new ArrayList<>();
        approvedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, approvedStudentNames);
        approvedStudentsListView.setAdapter(approvedAdapter);

        pendingStudentNames = new ArrayList<>();
        pendingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pendingStudentNames);
        pendingStudentsListView.setAdapter(pendingAdapter); // Set the adapter for pending students ListView

        // Retrieve student data from Firebase and filter by approval status
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child("students");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                approvedStudentNames.clear(); // Clear the list to prevent duplicates
                pendingStudentNames.clear(); // Clear the list for pending students

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String approvalStatus = userSnapshot.child("approvalStatus").getValue(String.class);

                    if (approvalStatus != null && approvalStatus.equals("approved")) {
                        String studentName = userSnapshot.child("name").getValue(String.class);
                        if (studentName != null) {
                            approvedStudentNames.add(studentName);
                        }
                    } else if (approvalStatus != null && approvalStatus.equals("pending")) {
                        String studentName = userSnapshot.child("name").getValue(String.class);
                        if (studentName != null) {
                            pendingStudentNames.add(studentName);
                        }
                    }
                }

                approvedAdapter.notifyDataSetChanged(); // Update the approved students ListView
                pendingAdapter.notifyDataSetChanged(); // Update the pending students ListView
                hideLoadingIndicator(); // Hide the loading indicator
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ApprovedStudents.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                hideLoadingIndicator(); // Hide the loading indicator in case of an error
            }
        });

        showLoadingIndicator(); // Show the loading indicator while data is loading
    }

    private void showLoadingIndicator() {
        loadingforApproved.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingforApproved.setVisibility(View.GONE);
    }
}
