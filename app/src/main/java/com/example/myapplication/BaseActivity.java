package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupBase;
import com.example.myapplication.model.PopupDate;
import com.example.myapplication.model.PopupHtml;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class BaseActivity extends AppCompatActivity {
    public static final int GALLERY_PERMISSION_REQUEST_CODE = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE  = 2;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    public LinearLayout Container;
    public TextView Header;
    public RadioGroup ButtonGroup;
    public RadioButton AddButton;
    public RadioButton EditButton;
    public RadioButton DeleteButton;

    public Function<String,Boolean> OnAction;

    @Override

    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Container = (LinearLayout) findViewById(R.id.container);
        Header = (TextView) findViewById(R.id.header);
        ButtonGroup = (RadioGroup) findViewById(R.id.icon_button_group);

        AddButton = (RadioButton) findViewById(R.id.radio_add);
        EditButton = (RadioButton) findViewById(R.id.radio_edit);
        DeleteButton = (RadioButton) findViewById(R.id.radio_delete);

        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick("Add",(RadioButton) view);
            }
        });
        EditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick("Edit",(RadioButton) view);
            }
        });

        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick("Delete",(RadioButton) view);
            }
        });

        Header.setText(getHeaderText());
        final BaseActivity activity = this;

        takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {



                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
                String newFileName = dateFormat.format(new Date());
                new DataService().upload(activity,photoFile,newFileName,Entity,Id, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                        String result = new String(responseBody);
                        System.out.println(result);
                        Bitmap imageBitmap;
                        try {
                             imageBitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), photoURI);
                        }
                        catch (IOException e){
                            imageBitmap = null;
                        }
                        onCapturedImage( RequestId ,imageBitmap,Long.parseLong(result));
                    }

                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                        String result = "Error found";
                        if(responseBody == null && error != null){
                            result = error.getMessage();
                        }
                        else if(responseBody != null){
                            result = new String(responseBody);
                        }
                        System.out.println(result);
                        Toast.makeText(BaseActivity.this, "Fail to pick image " + result, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Uri uri = result.getData().getData();

                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    File file;
                    try {
                        file = File.createTempFile("Image-AbuNaser",".jpg");
                    }
                    catch (IOException e){
                        Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();
                        return;
                    }
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
                    String newFileName = dateFormat.format(new Date());
                    new DataService().upload(activity,file,newFileName,Entity,Id, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                            String result = new String(responseBody);
                            System.out.println(result);
                            //if(imageListener != null)imageListener.getImage(imageBitmap,Long.parseLong(result));

                            onCapturedImage( RequestId ,imageBitmap,Long.parseLong(result));
                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                            String result = new String(responseBody);
                            System.out.println(result);
                            Toast.makeText(BaseActivity.this, "Fail to capture image " + result, Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch (IOException e){

                }
            }
        });
    }

    private  File photoFile;
    private Uri photoURI;
    private    void  ImageCapture(){
        // Camera permission is granted, you can proceed with camera-related operations
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getBaseContext(),"Error in image capture." + ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {

                photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //takePictureIntent.putExtra(MediaStore.,requestId);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    takePictureLauncher.launch(takePictureIntent);
                }catch (Exception err){
                    int a = 0;
                }



            }
        }
    }
    private    void  ImagePick(){
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }


    //private Uri photoThumbnailURI;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void  captureImage(int requestId,String entity,Long id,onGetImage listener){
        RequestId = requestId;
        if(requestId <0){
            Entity = entity;
            Id = id;
            imageListener  = listener;
            int galleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (galleryPermission == PackageManager.PERMISSION_GRANTED) {
                ImagePick();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            Entity = entity;
            Id = id;
            imageListener  = listener;

            int cameraPermission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                ImageCapture();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }
    public void onCapturedImage(int requestId,Bitmap image,Long id){
        if(imageListener != null)imageListener.getImage(requestId,image,id);
    }
    private String Entity;
    private long Id;

    private onGetImage imageListener = null;
    private int RequestId = 0;





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    ImageCapture();
                }
            }
        }
        else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePick();
            }
        }
    }
    public abstract void onButtonClick(String action, RadioButton button) ;
    public boolean viewNewButton(){
        return true;
    }
    public abstract String getHeaderText();
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getTitle().toString()){
            case "Home":
                intent = new Intent(this,MainActivity.class);
                break;
            case "Stock Receive":
                intent = new Intent(this,InvCheckInActivity.class);
                break;

            case "Test":
                intent = new Intent(this,TestActivity.class);
                break;
            default:
                super.onOptionsItemSelected(item);
                return  false;
        }
        startActivity(intent);
        return  true;
    }
    public static abstract class onGetImage{
        public abstract void getImage(int requestId,Bitmap image,long id) ;

    }


}
