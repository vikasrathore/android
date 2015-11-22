package com.cube26.trendingnow;

import com.cube26.celkonstore.R;
import com.cube26.trendingnow.util.Util;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TrendingAppUrlActivity extends Activity{

    EditText serverUrl, analyticsUrl;
    Button copyToAnalyticsBtn, submitBtn;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        setContentView(R.layout.trendingapp_url);

        serverUrl = (EditText)findViewById(R.id.editText1);
        analyticsUrl = (EditText) findViewById(R.id.editText2);

        serverUrl.setText(Util.API_URL);
        analyticsUrl.setText(Util.API_URL_ANALYTICS);

        copyToAnalyticsBtn = (Button) findViewById(R.id.button1);
        submitBtn = (Button) findViewById(R.id.button2);

        copyToAnalyticsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                analyticsUrl.setText(serverUrl.getText().toString());
            }
        });
        submitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Util.API_URL = serverUrl.getText().toString();
                Util.API_URL_ANALYTICS = analyticsUrl.getText().toString();
                Toast.makeText(TrendingAppUrlActivity.this, "Done", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
