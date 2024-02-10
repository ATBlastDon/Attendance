package com.example.attendace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRCodeScannerActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;
    private CaptureManager captureManager;
    private static final int CAMERA_PERMISSION_REQUEST = 123;
    private static final String TAG = "QRCodeScannerActivity"; // Add a tag for your logs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scanner);

        barcodeView = findViewById(R.id.scannerView);
        captureManager = new CaptureManager(this, barcodeView);
        captureManager.initializeFromIntent(getIntent(), savedInstanceState);
        captureManager.decode();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }

        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                String qrData = result.getText();
                String decryptedData = customDecrypt(qrData);

                if (decryptedData != null && decryptedData.startsWith("http")) {
                    // Modify the Intent to use "url" as the key
                    Intent webViewIntent = new Intent(QRCodeScannerActivity.this, Webview.class);
                    webViewIntent.putExtra("url", decryptedData); // Pass the scanned URL as an extra
                    startActivity(webViewIntent);

                } else {
                    Toast.makeText(QRCodeScannerActivity.this, "Scanned QR Code: Its not Correct QR Code", Toast.LENGTH_SHORT).show();
                }
                barcodeView.decodeSingle(this);
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });
    }

    private String customDecrypt(String encryptedData) {
        String decryptionKey = "AAAWEWEWWEERTYUI";
        char[] encryptedChars = encryptedData.toCharArray();
        char[] keyChars = decryptionKey.toCharArray();
        char[] decryptedChars = new char[encryptedChars.length];

        for (int i = 0; i < encryptedChars.length; i++) {
            decryptedChars[i] = (char) (encryptedChars[i] ^ keyChars[i % keyChars.length]);
        }

        return new String(decryptedChars);
    }

    @Override
    protected void onResume() {
        super.onResume();
        captureManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        captureManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captureManager.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        captureManager.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureManager.initializeFromIntent(getIntent(), null);
                captureManager.decode();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}