package com.example.qrcodemanager;

// Import the required libraries
        import android.Manifest;
        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.graphics.Bitmap;
        import android.graphics.drawable.BitmapDrawable;
        import android.media.MediaScannerConnection;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.view.View;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.Toast;

        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;

        import com.google.zxing.BarcodeFormat;
        import com.google.zxing.WriterException;
        import com.google.zxing.common.BitMatrix;
        import com.google.zxing.qrcode.QRCodeWriter;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.util.Random;

public class GenerateActivity extends Activity {
    // Declare variables
    private Button generateBtn;
    private EditText productName;
    private EditText productPrice;
    private ImageView qrCodeImage;
    private Button downloadBtn;

    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        // Initialize variables
        generateBtn = findViewById(R.id.generate_btn);
        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);
        qrCodeImage = findViewById(R.id.qr_code_image);
        downloadBtn = findViewById(R.id.download_btn);
        // Set onClickListener for the generate button
        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Generate the QR code from the product information
                String qrCodeData;
                    qrCodeData = generateQrCodeData();
                    if (qrCodeData != null) {
                        // Generate the QR code image
                        Bitmap qrCodeBitmap = generateQrCodeImage(qrCodeData);
                        if (qrCodeBitmap != null) {
                            // Display the QR code image on the screen
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            qrCodeImage.setImageBitmap(qrCodeBitmap);
                            downloadBtn.setVisibility(View.VISIBLE);
                            // Insert product in database
                            MyserverConnection sv=new MyserverConnection(GenerateActivity.this);
                            Product pd= new Product(productName.getText().toString().trim(),Double.parseDouble(productPrice.getText().toString().trim()),qrCodeData);
                            sv.addProduct(pd);
                        } else {
                            Toast.makeText(GenerateActivity.this, "Failed to generate QR code image", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(GenerateActivity.this, "Failed to generate QR code data", Toast.LENGTH_LONG).show();
                    }
                }
        });

        // Set onClickListener for the download button
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadQRCode();
            }
        });

    }
    // Method to generate the QR code data from the product information
    private String generateQrCodeData() {
        // Check if the product name and price are not empty
        if (productName.getText().toString().trim().length() == 0 || productPrice.getText().toString().trim().length() == 0) {
            Toast.makeText(GenerateActivity.this, "Please enter some valid data! ", Toast.LENGTH_LONG).show();
        }
        // Generate the QR code data as a string
        String qrCodeData;
        Random rand = new Random();
        int min = 1000000; // minimum 7-digit number
        int max = 9999999; // maximum 7-digit number
        int randomNumber = rand.nextInt((max - min) + 1) + min;
        qrCodeData = Integer.toString(randomNumber) ;
        return qrCodeData;

    }

    // Method to generate the QR code image from the QR code data
    private Bitmap generateQrCodeImage(String qrCodeData) {
        // Set the QR code size
        int qrCodeSize = 512;
        try {
            // Generate the QR code bit matrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);
            // Convert the bit matrix to a bitmap
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrCodeBitmap.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white));
                }
            }
            return qrCodeBitmap;
        } catch (WriterException e)
        {e.printStackTrace();}
        return null;
    }

    private void downloadQRCode() {
        // Check for external storage permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        } else {
            // Save QR code image to external storage
            String IMAGE_FILE_NAME = "qrcode.png";
            Bitmap qrCodeBitmap = ((BitmapDrawable) qrCodeImage.getDrawable()).getBitmap();
            File imageFile;
            File folder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);}
            else {
                folder= Environment.getExternalStorageDirectory();
            }
            // Check if file with same name already exists
            int i = 1;
            String newFileName = "qrcode.png";
            while (new File(folder, newFileName).exists()) {
                newFileName = IMAGE_FILE_NAME.replace(".png", "_" + i + ".png");;
                i++;
            }
            imageFile = new File(folder,newFileName);


            try {
                FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                MediaScannerConnection.scanFile(GenerateActivity.this, new String[]{imageFile.toString()}, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Show download success message
            Context context = getApplicationContext();
            CharSequence text = "QR Code downloaded successfully!";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadQRCode();
            } else {
                // Show permission denied message
                Context context = getApplicationContext();
                CharSequence text = "Storage permission denied!";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            }
        }
    }
}