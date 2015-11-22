package com.cube26.trendingnow;

import java.io.File;
import com.cube26.celkonstore.R;
import com.cube26.trendingnow.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.cube26.trendingnow.util.CLog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WidgetUpdateAlertActivity extends Activity{

    private Button buttonSbmt;
    private String apkFilePath;
    private File apkFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.update_alert_activity);
        this.setFinishOnTouchOutside(false);
        apkFilePath = getIntent().getStringExtra(Util.WIDGET_UPDATE_APK_PATH);
        if(apkFilePath!=null){
            apkFile = new File(apkFilePath);
        }

        buttonSbmt = (Button) findViewById(R.id.buttonSbmt);
        buttonSbmt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    CLog.d(Util.TAGC26, "Starting installer for widget update :: "+ intent);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    CLog.e(Util.TAGC26, "Exception ::" + e.getMessage());
                }
            }
        });
    }
}
