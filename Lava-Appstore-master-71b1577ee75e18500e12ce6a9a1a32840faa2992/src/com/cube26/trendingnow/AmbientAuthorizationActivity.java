/*
 * Copyright (C) 2014 Cyanogen, Inc.
 */

package com.cube26.trendingnow;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.widget.Toast.LENGTH_SHORT;
import static com.cyanogen.ambient.common.Scopes.USERINFO_SUMMARY;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.cube26.celkonstore.R;
import com.cube26.trendingnow.util.CLog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.cyanogen.ambient.auth.AuthToken;
import com.cyanogen.ambient.auth.AuthenticationApi;
import com.cyanogen.ambient.auth.AuthenticationServices;
import com.cyanogen.ambient.common.CyanogenAmbientUtil;
import com.cyanogen.ambient.common.api.AmbientApiClient;

/**
 * Simple activity example that exercises the authentication api.
 */
public class AmbientAuthorizationActivity extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "SimpleActivity";
    private static final int REQUEST_REFRESH_TOKEN = 1000;
    private static final int REQUEST_USER_AUTH = 2000;
    private static final int REQUEST_CHOOSE_ACCOUNT = 3000;
    private static final String ACCOUNT_TYPE_CYANOGEN = "com.cyanogen";

    private Spinner mSpinner;

    private AccountManager mAccountManager;
    private String mCurrentToken;

    private AmbientApiClient mClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_layout);

        mSpinner = (Spinner) findViewById(R.id.accounts);

        mAccountManager = AccountManager.get(this);

        mSpinner.setOnItemSelectedListener(this);
        // populateAccounts();

        int availability = CyanogenAmbientUtil.isCyanogenAmbientAvailable(this);
        String availabilityMsg;
        switch (availability) {
            case CyanogenAmbientUtil.SUCCESS: {
                availabilityMsg = "SUCCESS";
                mClient = new AmbientApiClient.Builder(this)
                        .addApi(AuthenticationServices.API)
                        .build();
                mClient.connect();
            }
                break;
            case CyanogenAmbientUtil.MISSING:
                availabilityMsg = "MISSING";
                break;
            case CyanogenAmbientUtil.VERSION_UPDATE_REQUIRED:
                availabilityMsg = "VERSION_UPDATE_REQUIRED";
                break;
            default:
                availabilityMsg = "UNKOWN";
        }
        // Toast.makeText(SimpleActivity.this, "availability == "
        // + availabilityMsg, LENGTH_SHORT).show();
        CLog.i(TAG, "availability == " + availabilityMsg);

        if (mClient == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            // set title
            alertDialogBuilder.setTitle("Authentication Error");

            // set dialog message
            alertDialogBuilder.setMessage(availabilityMsg)
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            finish();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE_CYANOGEN);
        if (accounts.length < 1) {
            chooseAccount();
        } else {
            getToken(accounts[0]);
        }

    }

    private void populateAccounts() {
        final Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE_CYANOGEN);
        ArrayAdapter<Account> adapter = new ArrayAdapter<Account>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, accounts);
        mSpinner.setAdapter(adapter);

        if (accounts.length < 1) {
            chooseAccount();
        } else {
            getToken(accounts[0]);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_REFRESH_TOKEN == requestCode) {
            if (data.getParcelableExtra(AuthenticationApi.KEY_AUTHTOKEN) != null) {
                AuthToken authToken = (AuthToken) data
                        .getParcelableExtra(AuthenticationApi.KEY_AUTHTOKEN);
                mCurrentToken = authToken.getToken();
                if (null != mCurrentToken) {
//                    Toast.makeText(SimpleActivity.this, "AuthToken\n" + mCurrentToken, LENGTH_SHORT)
//                            .show();
                    CLog.i(TAG, "AuthToken\n" + mCurrentToken);
                    //---
                    startTrendingApp();
                    finish();
                    //------
                }
            }
            if (data.getParcelableExtra(AuthenticationApi.KEY_USERRECOVERY_INTENT) != null) {
                Intent recoveryIntent = (Intent) data
                        .getParcelableExtra(AuthenticationApi.KEY_USERRECOVERY_INTENT);
                if (recoveryIntent != null) {
                    CLog.i(TAG, "Starting Authorization page");
                    AmbientAuthorizationActivity.this.startActivityForResult(recoveryIntent, REQUEST_USER_AUTH);
                    return;
                }
            } else {
                mCurrentToken = null;
                CLog.i(TAG,  "Failed to get refresh token\n"
                      + data.getStringExtra(AuthenticationApi.KEY_ERROR));
            }

//            Toast.makeText(
//                    SimpleActivity.this,
//                    "Failed to get refresh token\n"
//                            + data.getStringExtra(AuthenticationApi.KEY_ERROR), LENGTH_SHORT)
//                    .show();
        }
        if (REQUEST_USER_AUTH == requestCode) {
            if (RESULT_OK == resultCode) {
                // success now get the token
                AuthToken authToken = (AuthToken) data
                        .getParcelableExtra(AuthenticationApi.KEY_AUTHTOKEN);
                if (authToken != null) {
                    mCurrentToken = authToken.getToken();
                    CLog.i(TAG,  "AuthToken != null\nstarting app");
                    startTrendingApp();
                } else {
                    CLog.i(TAG,  "AuthToken == null finish");
                    mCurrentToken = null;
                    finish();
                }
//                Toast.makeText(SimpleActivity.this, "AuthToken\n" + mCurrentToken, LENGTH_SHORT)
//                        .show();
            } else {
                CLog.i(TAG,  "User denied access");
//                Toast.makeText(SimpleActivity.this, "User denied access", LENGTH_SHORT).show();
            }
        } else if (REQUEST_CHOOSE_ACCOUNT == requestCode) {
            CLog.i(TAG,  "User chose "
                    + data.getStringExtra(KEY_ACCOUNT_NAME));
//            Toast.makeText(SimpleActivity.this, "User chose "
//                    + data.getStringExtra(KEY_ACCOUNT_NAME), LENGTH_SHORT).show();
            populateAccounts();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mCurrentToken = null;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        mCurrentToken = null;
    }

    public void getToken(Account account) {
        // Account account = (Account) mSpinner.getSelectedItem();
        if (account == null) {
            chooseAccount();
            return;
        }

        if (mClient != null) {
            PendingIntent pi = createPendingResult(REQUEST_REFRESH_TOKEN, new Intent(),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AuthenticationServices.AuthenticationApi.getToken(mClient, account.name,
                    USERINFO_SUMMARY, new Bundle(), pi);
        }
    }

    private void startTrendingApp() {
        Intent startTrendingAppIntent = new Intent(AmbientAuthorizationActivity.this,
                TrendingAppActivity.class);
        startActivity(startTrendingAppIntent);
        finish();
    }

    public void invalidateToken(View view) {
        if (mClient != null && mCurrentToken != null) {
            AuthenticationServices.AuthenticationApi.clearToken(mClient, mCurrentToken);
            Toast.makeText(AmbientAuthorizationActivity.this, "AuthToken\n" + mCurrentToken
                    + "\nInvalidated", LENGTH_SHORT).show();
        } else {
            Toast.makeText(AmbientAuthorizationActivity.this, "No AuthToken", LENGTH_SHORT).show();
        }
    }

    public void chooseAccount() {
        Intent intent = AuthenticationServices.AuthenticationApi.newChooseAccountIntent();
        startActivityForResult(intent, REQUEST_CHOOSE_ACCOUNT);
    }
}
