package com.moumen.pharmazione.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.api.widget.Widget;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class Util {

    public final static int RC_SIGN_IN = 8;
    public final static int PICKFILE_REQUEST_CODE = 1;
    public final static int START_CAMERA_REQUEST_CODE = 2;
    public final static int START_ACTIVIY_VALIDATION = 515;
    public final static int START_ACTIVIY_BESOIN = 512;
    public final static int START_ACTIVIY_DON = 511;
    public final static String PATH = "med-dwa-pharmazion";
    public final static String EMPTY_IMAGE = "https://firebasestorage.googleapis.com/v0/b/data-278806.appspot.com/o/images%2Fempty.png?alt=media&token=7015119e-0356-4387-9f0f-3c807db65861";

    public  static  void load(ImageView imageView, String path){
        Glide.with(imageView.getContext())
                .load(path)
                //.centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public  static  void load(ImageView imageView, Uri path){
        Glide.with(imageView.getContext())
                .load(path)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public  static  void load(ImageView imageView, Bitmap path){
        Glide.with(imageView.getContext())
                .load(path)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public  static  void load(ImageView imageView, Drawable path){
        Glide.with(imageView.getContext())
                .load(path)
                //.centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public static void previewImages(ArrayList<String> imageList, Context con) {
        Album.gallery(con)
                .checkedList(imageList)
                .checkable(false)
                .widget(
                        Widget.newDarkBuilder(con)
                                .title("Preview")
                                .build()
                )
                .onResult(result -> {
                    // TODO If it is optional, here you can accept the results of user selection.
                })
                .start();
    }

    @BindingAdapter({"goneIf"})
    public static  void visibility(View v,Boolean gone){
        v.setVisibility(gone ? View.GONE : View.VISIBLE);
    }

    @BindingAdapter({"drawableRes"})
    public static void loadUrlImage(ImageView imageView,@DrawableRes int drawableRes)
    {
        Glide.with(imageView.getContext())
                .load(drawableRes)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    @SuppressLint("CheckResult")
    @BindingAdapter(
            value = {"url","drawable", "glideCenterCrop", "glideCircularCrop"},
            requireAll = false
    )
    public static void bindGlideSrc(ImageView imageView,@Nullable String url,@Nullable Drawable drawable,@Nullable boolean glideCenterCrop,@Nullable boolean glideCircularCrop) {
        Context context = imageView.getContext();
        RequestBuilder requestBuilder;
        if(url == null && drawable != null){
            requestBuilder = Glide.with(context).load(drawable).transition(DrawableTransitionOptions.withCrossFade());
        }
        else if( drawable == null && url != null){
            requestBuilder = Glide.with(context).load(url).transition(DrawableTransitionOptions.withCrossFade());
            //.placeholder(R.drawable.start_logo).error(R.drawable.start_logo);
        }
        else return;

        if (glideCenterCrop) {
            requestBuilder.centerCrop();
        }

        if (glideCircularCrop) {
            requestBuilder.circleCrop();
        }
        requestBuilder.into(imageView);

    }


    @SuppressLint("CheckResult")
    private static final RequestBuilder createGlideRequest(Context context, Drawable src, boolean centerCrop, boolean circularCrop) {
        RequestBuilder requestBuilder = Glide.with(context).load(src);
        if (centerCrop) {
            requestBuilder.centerCrop();
        }

        if (circularCrop) {
            requestBuilder.circleCrop();
        }

        return requestBuilder;
    }

    public static File writeFile( String baseDirectory, String filename, FileWritingCallback callback ) throws IOException {

        final File sd = Environment.getExternalStorageDirectory();
        String absFilename = baseDirectory + filename;
        File dest = new File(sd, absFilename);

        FileOutputStream out = new FileOutputStream(dest);

        callback.write( out );

        out.flush();
        out.close();
        return dest;
    }

    public interface FileWritingCallback {

        public void write(FileOutputStream out);
    }
//
//    @androidx.databinding.BindingAdapter({"goneIf"})
//    public static final void bindGoneIf(@NotNull View view, boolean gone) {
//        view.setVisibility(gone ? View.GONE : View.VISIBLE);
//    }
}
