package com.wang.powerfuledittext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mShakeBtn;
    private PowerfulEditText mAccountEd;
    private PowerfulEditText mPasswordEd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccountEd = (PowerfulEditText) findViewById(R.id.login_password);
        mPasswordEd = (PowerfulEditText) findViewById(R.id.login_account);
        mShakeBtn = (Button) findViewById(R.id.test_shake);
        mShakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAccountEd.startShakeAnimation();
                mPasswordEd.startShakeAnimation();
            }
        });
    }
}
