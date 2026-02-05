package com.example.myapplication.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.myapplication.Data.DataService;
import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import kotlin.text.Charsets;

public class PopupImage extends PopupBase<PopupImage, PopupImage.PopupImageArgs> {

    public static PopupImage create(String header,String guid){
        PopupImage popup = new PopupImage();
        popup.setArgs(new PopupImage.PopupImageArgs(header,guid));
        return popup;
    }
    public static PopupImage create(PopupImage.PopupImageArgs args){
        PopupImage popup = new PopupImage();
        popup.setArgs(args);
        return popup;
    }
    private String URLEncode(String data){

        if(data == null)return  "";
        try{
            return URLEncoder.encode(data, Charsets.UTF_8.name());
        }catch (UnsupportedEncodingException e){
            return  data;
        }
    }
    protected  ImageView imageView;

    @Override
    public void AddControls(LinearLayout container) {
        imageView = new ImageView(container.getContext());
        FlexboxLayout.LayoutParams lllP = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.MATCH_PARENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(lllP);






        container.addView(imageView);

        final String[] url = new String[1];

        url[0] = "MobileApi/GetImage?guid=" + URLEncode( getArgs().getGuid()) + "&size=0";
        System.out.println(url[0]);


        new DataService(getRootActivity()).get(url[0] , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                Bitmap bmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                imageView.setImageBitmap(bmp);
            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println(url[0] + "-" + error.getMessage());
            }
        });
    }
    public static class  PopupImageArgs extends PopupArgs<PopupImage.PopupImageArgs> {
        public PopupImageArgs(String header,String guid){
            super(header);
            setCancelButton("Close");
            Guid = guid;
        }
        private String Guid;

        public String getGuid() {
            return Guid;
        }
        public PopupImageArgs setGuid(String guid) {
            Guid = guid;
            return this;
        }
    }
}
