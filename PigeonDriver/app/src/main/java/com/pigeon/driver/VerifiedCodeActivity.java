package com.pigeon.driver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class VerifiedCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "VerifiedCodeActivity";
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private TextView text_show_phone;
    private String phoneNumber;
    private TextView digit_1,digit_2,digit_3,digit_4,digit_5,digit_6,digit_7,digit_8,digit_9,digit_0;
    private ImageView ditgit_backspace;
    private EditText edit_code;
    private TextWatcher mTextWatcher;
    private String mVerificationId;
    private int mCount;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verified_code_activity);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        phoneNumber = bundle.getString("phoneNumber");
        userId = bundle.getString("userId");
        Log.d(TAG, "phoneNumber : " + phoneNumber);

        Initialize();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Log.w(TAG, "Invalid request", e);

                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Log.w(TAG, "SMS quota overload", e);
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Log.d(TAG, "onCodeSent: verificationId: " + verificationId);
                mVerificationId = verificationId;
            }

        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,            // Phone number to verify
                60,                   // Timeout duration
                TimeUnit.SECONDS,       // Unit of timeout
                this,            // Activity (for callback binding)
                mCallbacks);
    }

    private void Initialize() {
        text_show_phone = findViewById(R.id.text_show_phone);
        text_show_phone.setText("Pleace type the verification code sent to \n" + phoneNumber);
        edit_code = findViewById(R.id.edit_code);

        //initialize keyboard
        digit_1 = findViewById(R.id.digit_1);
        digit_2 = findViewById(R.id.digit_2);
        digit_3 = findViewById(R.id.digit_3);
        digit_4 = findViewById(R.id.digit_4);
        digit_5 = findViewById(R.id.digit_5);
        digit_6 = findViewById(R.id.digit_6);
        digit_7 = findViewById(R.id.digit_7);
        digit_8 = findViewById(R.id.digit_8);
        digit_9 = findViewById(R.id.digit_9);
        digit_0 = findViewById(R.id.digit_0);
        ditgit_backspace = findViewById(R.id.ditgit_backspace);

        digit_1.setOnClickListener(this);
        digit_2.setOnClickListener(this);
        digit_3.setOnClickListener(this);
        digit_4.setOnClickListener(this);
        digit_5.setOnClickListener(this);
        digit_6.setOnClickListener(this);
        digit_7.setOnClickListener(this);
        digit_8.setOnClickListener(this);
        digit_9.setOnClickListener(this);
        digit_0.setOnClickListener(this);
        ditgit_backspace.setOnClickListener(this);

        edit_code.addTextChangedListener(mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCount++;
                Log.d(TAG, "mCount : " + mCount);
                if(mCount == 6){
                    //goto login
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, edit_code.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                    Log.d(TAG, "Login to Phone");
                    mCount = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            assert user != null;
                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User account phone deleted.");
                                    }
                                }
                            });

                            verifyComplete();

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void verifyComplete() {
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mUsersRef = mRootRef.child("Users").child(userId);
        mUsersRef.child("VerifiedPhone").setValue(1);

        Intent intent = new Intent(VerifiedCodeActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onClick(View v) {
        if(v.getTag() != null){
            edit_code.append(((TextView) v).getText());
        }

        if(v == ditgit_backspace){
            mCount = mCount - 2;  //1.delete click digit backspace 2.delete on text change
            Editable editable = edit_code.getText();
            int charCount = editable.length();
            if (charCount > 0) {
                editable.delete(charCount - 1, charCount);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        edit_code.removeTextChangedListener(mTextWatcher);
    }
}
