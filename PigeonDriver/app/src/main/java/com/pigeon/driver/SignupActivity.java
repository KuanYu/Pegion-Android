package com.pigeon.driver;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "SignupActivity";
    private ImageButton btn_back;
    private ImageView btn_image;
    private EditText input_name;
    private EditText input_email;
    private EditText input_phone;
    private Button btn_signup;
    private EditText input_password;
    private int RESULT_GALLERY = 99;
    private TextView text_waning_image, text_waning_name, text_waning_phone, text_waning_email, text_waning_password;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.signup_activity);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Initialize();
    }

    private void Initialize() {
        input_name = findViewById(R.id.input_name);
        input_email = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        input_phone = findViewById(R.id.input_phone);
        text_waning_image = findViewById(R.id.text_waning_image);
        text_waning_name = findViewById(R.id.text_waning_name);
        text_waning_password = findViewById(R.id.text_waning_password);
        text_waning_email = findViewById(R.id.text_waning_email);
        text_waning_phone = findViewById(R.id.text_waning_phone);
        btn_signup = findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(this);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        btn_image = findViewById(R.id.btn_image);
        btn_image.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onClick(View v) {
        if(v == btn_back){
            finish();
        }else if(v == btn_image){
            btn_image.setColorFilter(null);
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, RESULT_GALLERY);
            //setTag
        }else if(v == btn_signup){
            if(hasData()){
                mAuth.createUserWithEmailAndPassword(input_email.getText().toString(), input_password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    text_waning_image.setText(task.getException().getMessage());
                                }else{
                                    checkEmailVerification();
                                }
                            }
                        });
            }
        }
    }

    private void checkEmailVerification() {
        final FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            Log.d(TAG, "user getEmail : " + user.getEmail());
            user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        text_waning_email.setVisibility(View.GONE);
                        uploadImage();

                    }else{
                        text_waning_email.setVisibility(View.VISIBLE);
                        text_waning_email.setText(String.valueOf("Invalid email."));
                        mAuth.signOut();
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User account deleted.");
                                    }
                                }
                        });
                    }
                }
            });
        }else{
            Log.d(TAG, "user == null");
            input_email.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            text_waning_email.setVisibility(View.VISIBLE);
            text_waning_email.setText(String.valueOf("Invalid email."));
        }
    }

    private boolean hasData() {
        String name = input_name.getText().toString();
        String phone = input_phone.getText().toString();
        String email = input_email.getText().toString();
        String password = input_password.getText().toString();

        boolean hasName, hasPhone, hasEmail, hasPassword, hasImage;

        if(email.trim().isEmpty()) {
            input_email.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            text_waning_email.setVisibility(View.VISIBLE);
            text_waning_email.setText(String.valueOf("Invalid email."));
            hasEmail = false;
        }else {
            input_email.getBackground().mutate().setColorFilter(null);
            hasEmail = checkEmail(email);
        }


        if(password.trim().isEmpty()) {
            input_password.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            text_waning_password.setVisibility(View.VISIBLE);
            text_waning_password.setText(String.valueOf("Password must be at least 6 characters."));
            hasPassword = false;

        }else{
            input_password.getBackground().mutate().setColorFilter(null);
            hasPassword = checkPassword(password);
        }


        if(name.trim().isEmpty()) {
            input_name.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            text_waning_name.setVisibility(View.VISIBLE);
            text_waning_name.setText(String.valueOf("Please enter your name."));
            hasName = false;
        }else{
            input_name.getBackground().mutate().setColorFilter(null);
            text_waning_name.setVisibility(View.GONE);
            hasName = true;
        }


        if(phone.trim().isEmpty()) {
            input_phone.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            text_waning_phone.setVisibility(View.VISIBLE);
            text_waning_phone.setText(String.valueOf("Phone number must be at least 11 characters."));
            hasPhone = false;
        }else{
            input_phone.getBackground().mutate().setColorFilter(null);
            hasPhone = checkPhone(phone);
        }


        if(btn_image.getTag() == null) {
            btn_image.setColorFilter(getResources().getColor(R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
            text_waning_image.setVisibility(View.VISIBLE);
            text_waning_image.setText(String.valueOf("Please select your photo."));
            hasImage = false;
        }else{
            text_waning_image.setVisibility(View.GONE);
            hasImage = true;
        }

        return hasName && hasPhone && hasEmail && hasPassword && hasImage;
    }

    private boolean checkPhone(String ph) {
        if(ph.length() < 11) {
            input_phone.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            text_waning_phone.setVisibility(View.VISIBLE);
            text_waning_phone.setText(String.valueOf("Phone number must be at least 11 characters."));
            input_phone.setText("");
            return false;
        }else{
            text_waning_phone.setVisibility(View.GONE);
            return true;
        }
    }

    private boolean checkEmail(String em) {
        if(!(!TextUtils.isEmpty(em) && Patterns.EMAIL_ADDRESS.matcher(em).matches())){
            input_email.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            text_waning_email.setVisibility(View.VISIBLE);
            text_waning_email.setText(String.valueOf("Invalid email."));
            return false;
        }else{
            text_waning_email.setVisibility(View.GONE);
            return true;
        }
    }

    private boolean checkPassword(String pw) {
        if(pw.length() < 6){
            input_password.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            text_waning_password.setVisibility(View.VISIBLE);
            text_waning_password.setText(String.valueOf("Password must be at least 6 characters."));
            input_password.setText("");
            return false;
        }else{
            text_waning_password.setVisibility(View.GONE);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_GALLERY && resultCode == RESULT_OK){
            Picasso.with(this)
                    .load(data.getData())
                    .fit()
                    .centerCrop()
                    .transform(new CircleTransform())
                    .noFade()
                    .placeholder(R.color.white)
                    .error(R.color.black)
                    .into(btn_image);
            String uri = String.valueOf(data.getData());
            btn_image.setTag(uri);
        }
    }

    private void uploadImage() {
        //Get UID
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            final String uid = user.getUid();
            Log.d(TAG, "UID : " + uid);

            //Loading
            Loading.getInstance().dialogUploadImage(this, true);
            Loading.setStart();

            btn_image.setDrawingCacheEnabled(true);
            btn_image.buildDrawingCache();
            Bitmap bitmap = btn_image.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference folderRef = storageRef.child("users").child(uid);

            final UploadTask mUploadTask = folderRef.child(uid).putBytes(data, metadata);

            mUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Loading.getInstance().dialogUploadImage(SignupActivity.this, false);
                    Log.d(TAG, String.format("Failure: %s", exception.getMessage()));
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if(taskSnapshot.getDownloadUrl() != null) {
                        Loading.setComplete();
                        Log.d(TAG, "Upload Image Success");
                        //add data to database
                        DatabaseReference mUsersRef = mRootRef.child("Users").child(uid);
                        mUsersRef.child("Photo").setValue(String.valueOf(taskSnapshot.getDownloadUrl()));
                        insertDatabase();

                    }else {
                        Loading.getInstance().dialogUploadImage(SignupActivity.this, false);
                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    int progress = (int) ((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                    Loading.setProgress(progress);
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "OnPausedListener");
                }
            });
        }else{
            Log.d(TAG, "User == null");
        }
    }

    private void insertDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            String uid = user.getUid();
            Long timestamp = System.currentTimeMillis()/1000;
            DatabaseReference mUsersRef = mRootRef.child("Users").child(uid);
            mUsersRef.child("UID").setValue(uid);
            mUsersRef.child("Name").setValue(input_name.getText().toString());
            mUsersRef.child("Email").setValue(input_email.getText().toString());
            mUsersRef.child("Phone").setValue(input_phone.getText().toString());
            mUsersRef.child("Status").setValue(0);  //status pending
            mUsersRef.child("LastLogin").setValue(timestamp);
            mUsersRef.child("Role").setValue("driver");
            mUsersRef.child("VerifiedPhone").setValue(0);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Loading.closeDialog();
                    finish();
                }
            }, 1000);
        }
    }
}
