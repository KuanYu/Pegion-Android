package com.pigeon.driver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class VerifiedPhoneActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "VerifiedPhoneActivity";
    private FirebaseAuth mAuth;
    private ImageView btn_back;
    private ImageView image_verified;
    private EditText edit_country_code;
    private EditText edit_number_phone;
    private TextView text_verify;
    private Button btn_next;
    private TextView text_waning_phone;
    private static String COUNTRY_CODE = "+66";
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verified_phone_activity);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        userId = bundle.getString("userId");

        mAuth = FirebaseAuth.getInstance();
        Initialize();
    }

    private void Initialize() {
        image_verified = findViewById(R.id.image_verified);
        edit_country_code = findViewById(R.id.edit_country_code);
        edit_number_phone = findViewById(R.id.edit_number_phone);
        text_verify = findViewById(R.id.text_verify);
        text_waning_phone = findViewById(R.id.text_waning_phone);
        btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);

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
    public void onBackPressed() {
        super.onBackPressed();
        mAuth.signOut();
    }

    @Override
    public void onClick(View v) {
        if(v == btn_back){
            mAuth.signOut();
            finish();
        }else if(v == btn_next){
            if(!edit_number_phone.getText().toString().trim().isEmpty()) {
                if(edit_number_phone.getText().toString().length() == 9) {  //test thai 9 | lao 10
                    //test thai +66 | lao +856
                    String phoneNumber = COUNTRY_CODE + edit_number_phone.getText().toString();
                    edit_number_phone.setText("");
                    Intent intent = new Intent(this, VerifiedCodeActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                }else{
                    text_waning_phone.setVisibility(View.VISIBLE);
                    text_waning_phone.setText("Phone number must be at least 10 characters.");
                }
            }else{
                text_waning_phone.setVisibility(View.VISIBLE);
                text_waning_phone.setText("Please enter your phone number.");
            }
        }
    }
}
