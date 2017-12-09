package com.wang.powerfuledittext;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mShakeBtn;
    private PowerfulEditText mTestOne, mTestTwo, mTestThree, mTestFour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTestOne = (PowerfulEditText) findViewById(R.id.testOne);
        mTestTwo = (PowerfulEditText) findViewById(R.id.testTwo);
        mTestThree = (PowerfulEditText) findViewById(R.id.testThree);
        mTestFour = (PowerfulEditText) findViewById(R.id.testFour);
        mShakeBtn = (Button) findViewById(R.id.test_shake);
        mShakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTestOne.startShakeAnimation();
                mTestTwo.startShakeAnimation();
            }
        });
    }
}
