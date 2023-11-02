package com.example.myapplication.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class PopupImage extends PopupBase<PopupImage, PopupImage.PopupImageArgs> {

    public static PopupImage create(String header,Long id){
        PopupImage popup = new PopupImage();
        popup.setArgs(new PopupImage.PopupImageArgs(header,id));
        return popup;
    }
    public static PopupImage create(PopupImage.PopupImageArgs args){
        PopupImage popup = new PopupImage();
        popup.setArgs(args);
        return popup;
    }

    protected  ImageView imageView;

    @Override
    public void AddControls(LinearLayout container) {
        imageView = new ImageView(container.getContext());
        FlexboxLayout.LayoutParams lllP = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.MATCH_PARENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(lllP);






        container.addView(imageView);
        new DataService().get("refFile?size=0&id=" + getArgs().getId() , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                Bitmap bmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                imageView.setImageBitmap(bmp);
            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
        //PhotoViewAttacher pAttacher;
        //pAttacher = new PhotoViewAttacher(Your_Image_View);
        //pAttacher.update();

    }
    public static class  PopupImageArgs extends PopupArgs<PopupImage.PopupImageArgs> {
        public PopupImageArgs(String header,Long id){
            super(header);
            setCancelButton("Close");
            Id = id;
        }
        private Long Id;

        public Long getId() {
            return Id;
        }
        public PopupImageArgs setId(Long id) {
            Id = id;
            return this;
        }
    }
}
