package com.example.qrcodemanager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.text.DecimalFormat;

public class ScanActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 100;

    private Button scanBtn;
    private RadioButton cameraCb, galleryCb;
    private TextView pd_name;
    private TextView pd_price;
    private TextView pd_name_label;
    private TextView pd_price_label;
    private static final int REQUEST_CAMERA_PERMISSION = 102;
    private static final int REQUEST_READ_STORAGE = 112;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Initialize views
        scanBtn = findViewById(R.id.scan_btn);
        cameraCb = findViewById(R.id.camera_cb);
        galleryCb = findViewById(R.id.gallery_cb);
        pd_name= findViewById(R.id.product_name);
        pd_price= findViewById(R.id.product_price);
        pd_name_label=findViewById(R.id.product_name_label);
        pd_price_label=findViewById(R.id.product_price_label);

        // Set up click listener for scan button
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cameraCb.isChecked() && !galleryCb.isChecked()) {
                    Toast.makeText(ScanActivity.this, "Please choose an option to scan", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cameraCb.isChecked()) {
                    // Launch camera intent to scan QR code
                    scan_from_camera();

                } else {
                    // Launch file picker intent to choose image to scan
                    scan_from_gallery();

                }
            }
        });
    }
    private void scan_from_camera(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(MyCaptureActivity.class);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan a QR code");
            integrator.initiateScan();
        }
    }

    private void scan_from_gallery(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)  {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        }

        else {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);}
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IntentIntegrator.REQUEST_CODE:
                    // Retrieve QR code scan result from intent
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    if (result != null) {
                        // Check if the scan was successful
                        if (result.getContents() == null) {
                            // Scan was cancelled
                            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                        } else {
                            // Scan was successful, get the data
                            String qrData = result.getContents();
                            MyserverConnection sv = new MyserverConnection(ScanActivity.this);
                            ProgressDialog progressDialog = new ProgressDialog(ScanActivity.this);
                            progressDialog.setMessage("Retrieving product...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            Product pd= sv.getProduct(qrData);
                            progressDialog.dismiss();
                            if(pd!=null)
                            {   displayResult(pd);
                                Toast.makeText(this, "Sucess!", Toast.LENGTH_SHORT).show();}
                            else{
                                pd_name_label.setVisibility(View.INVISIBLE);
                                pd_name.setVisibility(View.INVISIBLE);
                                pd_price_label.setVisibility(View.INVISIBLE);
                                pd_price.setVisibility(View.INVISIBLE);
                            }

                            }}
                    else
                    {
                        Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();

                    }
                    break;

                case REQUEST_IMAGE_PICK:
                    // Retrieve image URI from intent and decode bitmap
                    Uri imageUri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (bitmap != null) {
                        // Decode QR code from bitmap and retrieve result
                        String resultFromBitmap = decodeQrCodeFromBitmap(bitmap);
                        MyserverConnection sv = new MyserverConnection(ScanActivity.this);
                        ProgressDialog progressDialog = new ProgressDialog(ScanActivity.this);
                        progressDialog.setMessage("Retrieving product...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        Product pd= sv.getProduct(resultFromBitmap);
                        progressDialog.dismiss();
                       if(pd!=null)
                        displayResult(pd);
                        else {
                            pd_name_label.setVisibility(View.INVISIBLE);
                            pd_name.setVisibility(View.INVISIBLE);
                            pd_price_label.setVisibility(View.INVISIBLE);
                            pd_price.setVisibility(View.INVISIBLE);
                        }

                    } else {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
      private  void displayResult(Product pd){
        pd_name.setText(pd.getName().trim());
        pd_name_label.setVisibility(View.VISIBLE);
        pd_name.setVisibility(View.VISIBLE);
        pd_price_label.setVisibility(View.VISIBLE);
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        String price= decimalFormat.format(pd.getPrice()) + " dt";
        pd_price.setText(price);
        pd_price.setVisibility(View.VISIBLE);


      }
    private String decodeQrCodeFromBitmap(Bitmap bitmap) {
        String qrCodeData = null;
        try {
            BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                    .setBarcodeFormats(Barcode.QR_CODE)
                    .build();
            if (!detector.isOperational()) {
                Toast.makeText(this, "Could not set up the detector!", Toast.LENGTH_SHORT).show();
                return null;
            }

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Barcode> barcodes = detector.detect(frame);

            if (barcodes.size() > 0) {
                Barcode barcode = barcodes.valueAt(0);
                qrCodeData = barcode.rawValue;
            }
        } catch (Exception e) {
            Log.e("QR Error", e.getMessage());
        }
        return qrCodeData;
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start scanning
                scan_from_camera();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        if (requestCode == REQUEST_READ_STORAGE ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start scanning
                scan_from_gallery();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Storage permission denied!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }





    }
}

