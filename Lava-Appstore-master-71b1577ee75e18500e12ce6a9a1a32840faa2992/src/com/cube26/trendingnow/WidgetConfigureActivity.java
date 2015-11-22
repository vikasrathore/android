package com.cube26.trendingnow;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.cube26.trendingnow.util.CLog;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cube26.celkonstore.R;

import com.cube26.trendingnow.util.Util;

public class WidgetConfigureActivity extends Activity {

	private int mAppWidgetId;
	private static final boolean SYSTEM_APP_BINDING = true;// CHange it to true
	private static final String TAG = "WidgetConfigureActivity";
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = WidgetConfigureActivity.this;
		Util.isWidgetConfigured = false;

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		// Commenting these calls as not to be included in release widget
		// DATA ASSIST

		// CL_GenUtil.runDC(WidgetConfigureActivity.this);
		// END DATA ASSIST
		// NOTIFICATION
		// try {
		// NotificationActivationReceiver
		// .setAlarmForNotificationAndActivation(mContext);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// END NOTIFICATION

		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);

			CLog.wtf(TAG, "mwidget id :: " + mAppWidgetId);
			Intent resultValue = new Intent();
			if (SYSTEM_APP_BINDING
					&& !Util.isPackageSystemApp(mContext,
							mContext.getPackageName())) {

				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				setResult(RESULT_CANCELED);
				Toast.makeText(mContext,
						"Widget doesn't have enough permissions.",
						Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			CLog.wtf(TAG, "everything is fine, updating app widget");

			Util.isWidgetConfigured = true;
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(mContext);

			RemoteViews views = new RemoteViews(mContext.getPackageName(),
					R.layout.widget_layout);
			appWidgetManager.updateAppWidget(mAppWidgetId, views);
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	}
}
