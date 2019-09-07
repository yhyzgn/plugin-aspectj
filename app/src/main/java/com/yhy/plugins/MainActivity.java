package com.yhy.plugins;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yhy.plugins.annotation.ClickIgnore;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-07 14:50
 * version: 1.0.0
 * desc   :
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvTest;
    private TextView tvTextDb;
    private TextView tvTextLambda;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTest = findViewById(R.id.tv_test);
        tvTextDb = findViewById(R.id.tv_test_db);
        tvTextLambda = findViewById(R.id.tv_test_lambda);

        tvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("单击了");
            }
        });

        tvTextDb.setOnClickListener(new View.OnClickListener() {
            @ClickIgnore
            @Override
            public void onClick(View v) {
                log("双击");
            }
        });

        tvTextLambda.setOnClickListener(v -> {
            log("拉姆达");
        });
    }

    private void log(String text) {
        Log.i("CLICK", text);
    }
}
