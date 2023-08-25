package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;

import android.Manifest;




public class MainActivity extends BaseActivity {
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ImageView imageView;

    private  void  ImageCapture(){
        // Camera permission is granted, you can proceed with camera-related operations
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureLauncher.launch(takePictureIntent);
        }
    }
    private  void  ImagePick(){
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        Button btnCamera = findViewById(R.id.btnCamera);
        final BaseActivity activity = this;
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cameraPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
                if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                    ImageCapture();
                } else {
                    activity.setPermissionGrantedListener(new onPermissionGrantedListener() {
                        @Override
                        public void onPermissionGranted(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                            if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                ImageCapture();
                            }
                        }
                    });
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                }
            }
        });

        Button btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int galleryPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (galleryPermission == PackageManager.PERMISSION_GRANTED) {
                    ImagePick();
                } else {
                    activity.setPermissionGrantedListener(new onPermissionGrantedListener() {
                        @Override
                        public void onPermissionGranted(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                            if (requestCode == GALLERY_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                ImagePick();
                            }
                        }
                    });
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
                }
            }
        });


        takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // Handle the image capture result here
                Bundle extras = result.getData().getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
            }
        });

        pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // Handle the image pick result here
                Uri selectedImageUri = result.getData().getData();
                imageView.setImageURI(selectedImageUri);
            }
        });
    }




    @Override
    public void onButtonClick(String action, RadioButton button) {

    }

    @Override
    public String getHeaderText() {
        return null;
    }
}