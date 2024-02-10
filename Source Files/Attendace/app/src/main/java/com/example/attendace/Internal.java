package com.example.attendace;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
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

public class Internal extends AppCompatActivity {
    Button scan, yourattendance, aboutteam, yourprofile;
    TextView userNameTextView;
    ImageView InternalProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal);

        scan = findViewById(R.id.qr);
        yourattendance = findViewById(R.id.atten);
        aboutteam = findViewById(R.id.AboutTeam);
        yourprofile = findViewById(R.id.YourProfile);
        userNameTextView = findViewById(R.id.userNameTextView);
        InternalProfile = findViewById(R.id.InternalProfile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();

            checkUserRollNumber(userEmail);
        } else {
            userNameTextView.setText("Guest");
        }
    }

    private void checkUserRollNumber(String userEmail) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child("students");
        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String profilePhotoUrl = userSnapshot.child("profilePhotoUrl").getValue(String.class);
                        String rollNo = userSnapshot.child("rollNo").getValue(String.class);
                        if (rollNo != null && !rollNo.isEmpty()) {
                            userNameTextView.setText(userSnapshot.child("name").getValue(String.class));
                        } else {
                            userNameTextView.setText(userSnapshot.child("name").getValue(String.class) + "  \uD83D\uDE4B\uD83C\uDFFB\u200D♂ ");
                        }
                        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                            Glide.with(Internal.this)
                                    .load(profilePhotoUrl)
                                    .apply(RequestOptions.circleCropTransform()) // Optional: Apply circular cropping
                                    .into(InternalProfile);
                        }
                    }
                } else {
                    userNameTextView.setText("Welcome, Guest");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Internal.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void yourattendance(View view) {
        String spreadsheetUrl = "https://docs.google.com/spreadsheets/d/1QiLAG5u_Xz5qyGbPWUjNAOHYrKewS4c_K_ghoUuI5Pk/edit#gid=63758129";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(spreadsheetUrl));
        startActivity(intent);
    }

    public void aboutteam(View view) {
        startActivity(new Intent(this, AboutTeam.class));
    }

    public void yourprofile(View view) {
        startActivity(new Intent(this, YourProfile.class));
    }

    public void scan(View view) {
        if (isNetworkConnectedToDesiredIP()) {
            // Open the QR code scanner activity
            startActivity(new Intent(this, QRCodeScannerActivity.class));
        } else {
            // Show a message that the device is not connected to the desired network
            Toast.makeText(this, "Connect to the correct network or IP address", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNetworkConnectedToDesiredIP() {
        String desiredIpAddressPrefix = "192.168.1"; // Change this to the desired prefix

        // Get the device's current IP address
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        // Convert the IP address integer to a string
        String deviceIpAddress = String.format(
                "%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff)
        );

        Log.d("NetworkStatus", "Device IP Address: " + deviceIpAddress); // Log the retrieved IP address

        // Check if the first three sets of the device's IP address match the desired prefix
        return deviceIpAddress.startsWith(desiredIpAddressPrefix);
    }
}
