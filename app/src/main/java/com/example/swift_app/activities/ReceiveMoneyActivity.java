package com.example.swift_app.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.utils.SessionManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ReceiveMoneyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_money);

        SessionManager sessionManager = new SessionManager(this);
        
        TextView tvEmail = findViewById(R.id.tvUserEmail);
        TextView tvId = findViewById(R.id.tvUserId);
        ImageView ivQrCode = findViewById(R.id.ivQrCode);
        
        String userId = sessionManager.getUserId();
        tvEmail.setText(sessionManager.getUserEmail());
        tvId.setText("ID: " + userId);

        generateQrCode(userId, ivQrCode);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnShare).setOnClickListener(v -> {
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Send money to me on SwiftX: " + sessionManager.getUserEmail());
            startActivity(android.content.Intent.createChooser(shareIntent, "Share with"));
        });
    }

    private void generateQrCode(String data, ImageView imageView) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}

