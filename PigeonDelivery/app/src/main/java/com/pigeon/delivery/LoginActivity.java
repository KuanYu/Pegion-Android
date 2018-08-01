package com.pigeon.delivery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * Created by Chalitta Khampachua on 02-Feb-18.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private String TAG = "LoginFragment";
    private Context mContext;
    private SignInButton loginButtonGoogle;
    private static final int RC_SIGN_IN_GOOGLE = 9001;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRootRef;
    private Typeface fonts;
    private TextView text_other_login;
    private GoogleSignInClient mGoogleSignInClient;
    private LinearLayout rootView;
    private TextView input_email;
    private TextView input_password;
    private TextView text_register;
    private Button btn_login;
    private TextView text_waning;
    private TextView text_forget;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mContext = LoginActivity.this;
        setContentView(R.layout.login_activity);
        fonts = Typeface.createFromAsset(mContext.getAssets(), "fonts/ComfortaaRegular.ttf");

        InitializaLogin();
        Initialize();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if(user.isEmailVerified()) {
                        loginSuccess(user.getUid());
                    }else{
                        text_waning.setVisibility(View.VISIBLE);
                        text_waning.setText("Please check your email for verification.");
                    }
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };
    }

    private void Initialize() {
        rootView = findViewById(R.id.rootView);
        text_other_login = findViewById(R.id.text_other_login);
        text_other_login.setVisibility(View.GONE);
        text_waning = findViewById(R.id.text_waning);
        input_email = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        text_forget = findViewById(R.id.text_forget);
        text_forget.setOnClickListener(this);
        text_register = findViewById(R.id.text_register);
        text_register.setOnClickListener(this);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);

    }

    private void InitializaLogin() {

        //Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

        //Initialize google login
        loginButtonGoogle = (SignInButton) findViewById(R.id.login_button_google);
        loginButtonGoogle.setSize(SignInButton.SIZE_WIDE);
        loginButtonGoogle.setOnClickListener(this);
        setTextButtonGoogle(loginButtonGoogle, "Sign in with Google");

    }

    private void updateUI(FirebaseUser user) {
        text_other_login.setVisibility(View.GONE);
        if (user != null) {
            if(user.isEmailVerified()) {
                rootView.setVisibility(View.GONE);
            }else{
                rootView.setVisibility(View.VISIBLE);
            }
        } else {
            rootView.setVisibility(View.VISIBLE);
        }
    }

    private void setTextButtonGoogle(SignInButton loginButtonGoogle, String buttonText) {
        for (int i = 0; i < loginButtonGoogle.getChildCount(); i++) {
            View v = loginButtonGoogle.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                tv.setTypeface(fonts);
                tv.setTextSize(16);
                tv.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);

                return;
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(input_password != null && text_waning != null) {
            input_password.setText("");
            text_waning.setVisibility(View.GONE);
        }
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) mRootRef.child("Users").child(user.getUid()).removeEventListener(mValueEventListener);
    }

    @Override
    public void onClick(View v) {
        if (v == loginButtonGoogle) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
        }else if(v == btn_login){
            String email = input_email.getText().toString();
            String password = input_password.getText().toString();
            if(email.trim().isEmpty()) hasEmail();
            if(password.trim().isEmpty()) hasPassword();
            if(!email.trim().isEmpty() && isValidEmail(email) && !password.trim().isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            String text_task = task.getException().getMessage();
                            text_waning.setVisibility(View.VISIBLE);
                            text_waning.setText(text_task);
                            input_password.setText("");
                        } else {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            if(user.isEmailVerified()) {
                                loginSuccess(user.getUid());
                            }else{
                                text_waning.setVisibility(View.VISIBLE);
                                text_waning.setText("Please check your email for verification.");
                            }
                        }
                    }
                });
            }else{
                text_waning.setVisibility(View.VISIBLE);
                text_waning.setText("Please check your email and password.");
                input_password.setText("");
                input_email.setText("");
                input_password.clearFocus();
                input_email.setFocusable(true);
            }
        }else if(v == text_register){
            text_waning.setVisibility(View.GONE);
            input_password.setText("");
            input_email.setText("");
//            Intent intent = new Intent(mContext, SignupActivity.class);
//            startActivity(intent);

        }else if(v == text_forget){
//            Intent intent = new Intent(mContext, ForgetPassword.class);
//            startActivity(intent);
        }
    }

    private void hasEmail(){
        input_email.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        text_waning.setVisibility(View.VISIBLE);
        text_waning.setText("Please enter your email.");
    }

    private void hasPassword(){
        input_password.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        text_waning.setVisibility(View.VISIBLE);
        text_waning.setText("Please enter your password.");
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    //Callback Google Failed
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(mContext, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }


    //Callback Facebook and Callback Google
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:requestCode " + resultCode);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            Log.d(TAG, "onActivityResult:Sing in with Google");

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
               Log.e(TAG, "signInResult:failed code=" +  e.getStatusCode());
               updateUI(null);
            }

        }else{
            updateUI(null);
        }
    }

    //Login complete Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //setTranslate();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.d(TAG, "firebaseAuthWithGoogle:onComplete_fail" + task.getException().getMessage());
                    text_other_login.setVisibility(View.VISIBLE);
                    text_other_login.setText("* Please login with Facebook");
                    //objLoading.loading(false);

                } else {
                    Log.d(TAG, "firebaseAuthWithGoogle:onComplete_success");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String mUserName = user.getDisplayName();
                        String mEmail = user.getEmail();
                        String mPhotoUrl = String.valueOf(user.getPhotoUrl());
                        String mPhone = user.getPhoneNumber();
                        String mUserID = user.getUid();
                        handleRegister(mUserID, mUserName , mEmail, mPhotoUrl,  mPhone);
                    }

                    updateUI(user);
                }
            }
        });
    }

    private void handleRegister(String mUserID, String mUserName, String mEmail, String mPhotoUrl, String mPhone) {

        //Add and Update data basic
        Long timestamp = System.currentTimeMillis()/1000;
        final DatabaseReference mUsersRef = mRootRef.child("Users").child(mUserID);
        mUsersRef.child("UID").setValue(mUserID);
        mUsersRef.child("Name").setValue(mUserName);
        mUsersRef.child("Email").setValue(mEmail);
        mUsersRef.child("Photo").setValue(mPhotoUrl);
        mUsersRef.child("Phone").setValue(mPhone);
        mUsersRef.child("Status").setValue(0);  //status pending
        mUsersRef.child("LastLogin").setValue(timestamp);
        mUsersRef.child("Role").setValue("driver");
        mUsersRef.child("VerifiedPhone").setValue(0);

        //loginSuccess(mUserID);
    }

    private void loginSuccess(String uid){
        isVerifiedPhone(uid);
    }

    private void isVerifiedPhone(final String uid) {
        mRootRef.child("Users").child(uid).addValueEventListener(mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> mapObj = (Map<String, Object>) dataSnapshot.getValue();
                    assert mapObj != null;
                    if(mapObj.containsKey("VerifiedPhone")){
                        String getVerify = mapObj.get("VerifiedPhone").toString();
                        int verified = Integer.parseInt(getVerify);
                        if(verified == 1){
//                            Intent intent = new Intent(mContext, HomeActivity.class);
//                            intent.putExtra("userId",uid);
//                            startActivity(intent);
//                            finish();
                        }else{
                            //intent verified phone activity
//                            Intent intent = new Intent(mContext, VerifiedPhoneActivity.class);
//                            intent.putExtra("userId",uid);
//                            startActivity(intent);
                        }
                    }else{
                        //intent verified phone activity
//                        Intent intent = new Intent(mContext, VerifiedPhoneActivity.class);
//                        intent.putExtra("userId",uid);
//                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
