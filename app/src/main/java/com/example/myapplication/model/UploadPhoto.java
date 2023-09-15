package com.example.myapplication.model;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.BaseActivity;

import java.util.ArrayList;

public class UploadPhoto {
    public UploadPhoto(BaseActivity activity){
        Activity = activity;
        takePictureLauncher = Activity.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                // Handle the image capture result here
                Bundle extras = result.getData().getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //imageView.setImageBitmap(imageBitmap);
            }
        });

        pickImageLauncher = Activity.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                // Handle the image pick result here
                Uri selectedImageUri = result.getData().getData();
                //imageView.setImageURI(selectedImageUri);
            }
        });


    }
    private  BaseActivity Activity;
    public static final int GALLERY_PERMISSION_REQUEST_CODE = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE  = 2;

    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;


    private  void  ImageCapture(){
        // Camera permission is granted, you can proceed with camera-related operations
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(Activity.getPackageManager()) != null) {
            takePictureLauncher.launch(takePictureIntent);
        }
    }
    private  void  ImagePick(){
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }


    public void  PickImage(){



        ArrayList<DataService.Lookup> lookups = new ArrayList<DataService.Lookup>();
        DataService.Lookup l = new DataService.Lookup(1L,"Take Photo");

        lookups.add(new DataService.Lookup(1L,"Pick Photo From Gallery"));
        lookups.add(new DataService.Lookup(2L,"Take Photo By Camera"));

        /*
        new PopupLookup("Image Source", lookups, new PopupLookup.onFormPopupLookupListener() {
            @Override
            public boolean onPick(DataService.Lookup lookup) {
                if(lookup.Id == 1L){
                    int galleryPermission = ContextCompat.checkSelfPermission(Activity, Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (galleryPermission == PackageManager.PERMISSION_GRANTED) {
                        ImagePick();
                    } else {



                        ActivityCompat.requestPermissions(Activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
                        galleryPermission = ContextCompat.checkSelfPermission(Activity, Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (galleryPermission == PackageManager.PERMISSION_GRANTED) {
                            ImagePick();
                        }
                    }



                }
                else if(lookup.Id == 2L){

                }


                return true;
            }
        });
        */

    }



}
