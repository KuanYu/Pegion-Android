package com.pigeon.driver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity implements View.OnClickListener {

    private TextView text_success;
    private TextView text;
    private TextView text_email;
    private EditText input_email;
    private Button btn_reset;
    private ImageButton btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password_activity);

        text_success = (TextView) findViewById(R.id.text_success);
        text = (TextView) findViewById(R.id.text);
        text_email = (TextView) findViewById(R.id.text_email);
        input_email = (EditText) findViewById(R.id.input_email);
        btn_reset = (Button) findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(this);
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v == btn_reset){
            text_success.setVisibility(View.GONE);
            FirebaseAuth auth = FirebaseAuth.getInstance();
            final String emailAddress = input_email.getText().toString();
            if(!input_email.getText().toString().trim().isEmpty() && isValidEmail(emailAddress)){
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                if (task.isSuccessful()) {
                                    text.setText("Please check reset password link in "+emailAddress);
                                    text_success.setVisibility(View.VISIBLE);
                                    input_email.setText("");
                                }
                            }
                        });
            }
        }else if(v == btn_back){
            finish();
        }
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
