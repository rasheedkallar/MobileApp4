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

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupBase;
import com.example.myapplication.model.PopupDate;
import com.example.myapplication.model.PopupForm;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class BaseActivity extends AppCompatActivity {
    public static  final int TAKE_IMAGE_FROM_CAMERA = 1;
    public static  final int TAKE_IMAGE_FROM_GALLERY = -1;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE  = 2;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    public LinearLayout Container;
    public ArrayList<Control.ControlBase> Controls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            Controls = (ArrayList<Control.ControlBase>) savedInstanceState.getSerializable("Controls");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Container = (LinearLayout) findViewById(R.id.container);

        final BaseActivity activity = this;

        takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
                String newFileName = dateFormat.format(new Date());
                new DataService().upload(activity,image_file,newFileName,image_entityName,image_entity_id, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                        String result = new String(responseBody);
                        System.out.println(result);
                        Bitmap imageBitmap;
                        try {
                             imageBitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), image_uri);
                        }
                        catch (IOException e){
                            imageBitmap = null;
                        }
                        onCapturedImage( image_action ,imageBitmap,image_entityName,image_entity_id,Long.parseLong(result));
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
                    new DataService().upload(activity,file,newFileName,image_entityName,image_entity_id, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                            String result = new String(responseBody);
                            System.out.println(result);
                            //if(imageListener != null)imageListener.getImage(imageBitmap,Long.parseLong(result));

                            onCapturedImage( image_action ,imageBitmap,image_entityName,image_entity_id,Long.parseLong(result));
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
        if(Controls != null){

            for (int i = 0; i < Controls.size(); i++) {
                Controls.get(i).addView(Container);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        String image_uri_string = null;
        if(image_uri != null)image_uri_string = image_uri.getPath();
        savedInstanceState.putString("image_uri_string", image_uri_string);

        String image_file_string = null;
        if(image_file != null)
            image_file_string =image_file.getAbsolutePath();
        savedInstanceState.putString("image_file_string", image_file_string);

        savedInstanceState.putLong("image_entity_id",image_entity_id);
        savedInstanceState.putInt("image_action",image_action);
        savedInstanceState.putString("image_entityName",image_entityName);

        savedInstanceState.putSerializable("Controls",Controls);




    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String image_uri_string = savedInstanceState.getString("image_uri_string");
        image_uri = null;
        if(image_uri_string != null)
            image_uri = Uri.parse(image_uri_string);


        String image_file_string = savedInstanceState.getString("image_file_string");
        image_file = null;
        if(image_file_string != null)
            image_file = new File(image_file_string);

        image_entity_id = savedInstanceState.getLong("image_entity_id");
        image_action = savedInstanceState.getInt("image_action");
        image_entityName = savedInstanceState.getString("image_entityName");

        Controls = (ArrayList<Control.ControlBase>) savedInstanceState.getSerializable("Controls");


    }


    private long image_entity_id;
    private String image_entityName;
    private int image_action = 0;
    private  File image_file;
    private Uri image_uri;
    private    void  ImageCapture(){
        // Camera permission is granted, you can proceed with camera-related operations
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                image_file = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getBaseContext(),"Error in image capture." + ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            if (image_file != null) {

                image_uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", image_file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
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



    protected String getEntityName(){
        String name = this.getClass().getName();
        int dot = name.lastIndexOf('.');
        if(dot >0)name= name.substring(dot + 1);
        if(name.endsWith("Activity"))name = name.substring(0,name.length() - 8);
        return name;
    }



    public void  captureImage(int action,String entityName,Long entityId){
        image_action = action;
        image_entityName  = entityName;
        if(image_action <0){
            image_entity_id = entityId;
            int galleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (galleryPermission == PackageManager.PERMISSION_GRANTED) {
                ImagePick();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            image_entity_id = entityId;

            int cameraPermission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                ImageCapture();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }

    public ArrayList<PopupBase> Popups = new ArrayList<PopupBase>();
    public void onCapturedImage(int action,Bitmap image,String entityName,Long entityId,Long id){
        for (int i = 0; i < Popups.size(); i++) {
            if(PopupForm.class.isAssignableFrom(Popups.get(i).getClass())){
                PopupForm form = (PopupForm)Popups.get(i);
                form.onCapturedImage(action,image,entityName,entityId,id);
            }



        }
    }


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



}
