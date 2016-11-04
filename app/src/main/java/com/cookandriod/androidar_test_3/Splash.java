package com.cookandriod.androidar_test_3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by KANG on 2016-09-23.
 */
public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Handler hd = new Handler();
        hd.postDelayed(new Runnable() {

            @Override
            public void run() {

                startActivity(new Intent(getBaseContext(), org.mixare.MixView.class));
                finish();       // 3 초후 이미지를 닫아버림
            }
        }, 3000);
    }
}
