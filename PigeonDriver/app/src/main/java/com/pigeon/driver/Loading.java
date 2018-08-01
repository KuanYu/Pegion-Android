package com.pigeon.driver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Vector;

/**
 * Created by Chalitta Khampachua on 07-Feb-18.
 */

public class Loading {

    private static String TAG = "Loading";
    private static Vector<Dialog> vector_dialogs_loading = new Vector<Dialog>();
    private static Loading instance;
    private static TextView text_status;
    private static ImageView mark_image;
    private static ProgressBar progressbar;
    private static Dialog dialog_upload;

    public static Loading getInstance(){
        if(instance == null){
            instance = new Loading();
        }
        return instance;
    }

    public void dialogUploadImage(Context context, boolean isShow){
        if (isShow) {
            dialog_upload = new Dialog(context);
            dialog_upload.setCanceledOnTouchOutside(false);
            dialog_upload.getWindow();
            dialog_upload.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog_upload.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog_upload.getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            dialog_upload.setContentView(R.layout.dialog_upload);

            text_status = (TextView) dialog_upload.findViewById(R.id.text_status);
            mark_image = (ImageView) dialog_upload.findViewById(R.id.mark_image);
            progressbar = (ProgressBar) dialog_upload.findViewById(R.id.progressbar);

            if (!((Activity) context).isFinishing()) {
                vector_dialogs_loading.add(dialog_upload);
                dialog_upload.show();
            }

        } else {
            closeDialog();
        }
    }

    public static void setStart(){
        text_status.setText(String.valueOf("Uploading"));
        mark_image.setImageResource(R.drawable.ic_upload);
        progressbar.setProgress(10);
    }

    public static void setProgress(int i) {
        progressbar.setProgress(i);
    }

    public static void setComplete(){
        text_status.setText(String.valueOf("Complete"));
        mark_image.setImageResource(R.drawable.ic_complete);
        progressbar.setProgress(100);
    }

    public static void closeDialog(){
        if(!vector_dialogs_loading.isEmpty()) {
            Log.d(TAG, "*** dialog vector : " + vector_dialogs_loading.toString() + "***");
            for (Dialog dialog : vector_dialogs_loading) {
                if (dialog.isShowing()) {
                    if (dialog != null) {
                        try {
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.getMessage();
                        }
                    }
                }
            }

            vector_dialogs_loading.clear();
        }
    }
}
