/*
 * Copyright (C) 2014 Cyanogen, Inc.
 */

package com.cube26.trendingnow;


import static android.widget.Toast.LENGTH_SHORT;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.cube26.celkonstore.R;
import com.cube26.trendingnow.util.Util;
import com.cyanogen.ambient.auth.AuthToken;
import com.cyanogen.ambient.auth.AuthTokenResult;
import com.cyanogen.ambient.auth.AuthenticationApi;
import com.cyanogen.ambient.auth.AuthenticationServices;
import com.cyanogen.ambient.common.CyanogenAmbientUtil;
import com.cyanogen.ambient.common.api.AmbientApiClient;


/**
 *  Simple activity example that exercises the authentication api.
 */
public class AmbientAuthActivity extends Activity implements AdapterView.OnItemSelectedListener {
    private static final int REQUEST_USER_AUTH = 2000;
    private static final int REQUEST_CHOOSE_ACCOUNT = 3000;
    private static final String ACCOUNT_TYPE_CYANOGEN = "com.cyanogen";

    private Spinner mSpinner;

    private AccountManager mAccountManager;
    private String mCurrentToken;

    private AmbientApiClient mClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ambient_auth_layout);

        //        mSpinner = (Spinner) findViewById(R.id.accounts);

        mAccountManager = AccountManager.get(this);

        //        mSpinner.setOnItemSelectedListener(this);
        //        populateAccounts();
        //        
        //        if(Util.getAuthTokenFromSharedPref(this).trim().length()>0){
        //            startTrendingApp();
        //        }
        int availability = CyanogenAmbientUtil.isCyanogenAmbientAvailable(this);
        String availabilityMsg;
        switch(availability) {
            case CyanogenAmbientUtil.SUCCESS: {
                availabilityMsg = "";
                mClient = new AmbientApiClient.Builder(this)
                .addApi(AuthenticationServices.API)
                .build();
                mClient.connect();
            }
            break;
            case CyanogenAmbientUtil.MISSING:
                availabilityMsg = "Missing Cynogen SDK";
                break;
            case CyanogenAmbientUtil.VERSION_UPDATE_REQUIRED:
                availabilityMsg = "Version update required for Cynogen SDK"; 
                break;
            default:
                availabilityMsg = "Unknown Error with Cynogen";
        }
        if(availabilityMsg.length()>0){
            Toast.makeText(AmbientAuthActivity.this, "Ambient Authentication Error : "
                    + availabilityMsg, LENGTH_SHORT).show();
        }
        if(mClient==null){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            // set title
            alertDialogBuilder.setTitle("Authentication Error");

            // set dialog message
            alertDialogBuilder.setMessage(availabilityMsg)
            .setCancelable(false)
            .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    // if this button is clicked, close
                    // current activity
                    finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE_CYANOGEN);
        if(accounts.length<1){
            chooseAccount();
        }else{
            getToken(accounts[0]);
        }
    }

//    private void populateAccounts() {
//        final Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE_CYANOGEN);
//        ArrayAdapter<Account> adapter = new ArrayAdapter<Account>(this,
//                android.R.layout.simple_list_item_1, android.R.id.text1, accounts);
//        mSpinner.setAdapter(adapter);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_USER_AUTH == requestCode) {
            if (RESULT_OK == resultCode) {
                // success now get the token
                AuthToken authToken = (AuthToken) data.getParcelableExtra(AuthenticationApi.KEY_AUTHTOKEN);
                if (authToken != null) {
                    mCurrentToken = authToken.getToken();
                    Toast.makeText(AmbientAuthActivity.this, "AuthToken\n" + mCurrentToken, LENGTH_SHORT).show();
                    Util.setAuthTokenInSharedPref(AmbientAuthActivity.this, mCurrentToken);
                    startTrendingApp();
                } else {
                    mCurrentToken = null;
                }
            } else {
                Toast.makeText(AmbientAuthActivity.this, "User denied access", LENGTH_SHORT).show();
            }
        } else if (REQUEST_CHOOSE_ACCOUNT == requestCode) {
            //            Toast.makeText(SimpleActivity.this, "User chose "
            //                    + data.getStringExtra(KEY_ACCOUNT_NAME), LENGTH_SHORT).show();
            //populateAccounts();
        }
    }

    private void startTrendingApp(){
        Intent startTrendingAppIntent = new Intent(AmbientAuthActivity.this, TrendingAppActivity.class);
        startActivity(startTrendingAppIntent);
        finish();
    }
    public void getToken(Account account) {
        //Account account = (Account) mSpinner.getSelectedItem();
        if (account == null) {
            chooseAccount();
            return;
        }

        if (mClient != null) {
            AuthenticationServices.AuthenticationApi.getToken(mClient,account.name, "", new Bundle(), new AuthTokenResult() {
                @Override
                public void onTokenResult(AuthToken token) {
                    mCurrentToken = token.getToken();
                    if (null != mCurrentToken) {
                        Toast.makeText(AmbientAuthActivity.this, "AuthToken\n" + mCurrentToken, LENGTH_SHORT).show();
                        return;
                    }
                }

                @Override
                public void onUserRecoveryNeeded(Intent intent) {
                    startActivityForResult(intent, REQUEST_USER_AUTH);
                }

                @Override
                public void onUnrecoverableError(String error) {
                    Toast.makeText(AmbientAuthActivity.this, "Unrecoverable Error " + error, LENGTH_SHORT).show();
                }
            });
        }
    }
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mCurrentToken = null;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        mCurrentToken = null;
    }
    public void invalidateToken(View view) {
        if (mClient != null && mCurrentToken != null) {
            AuthenticationServices.AuthenticationApi.clearToken(mClient,mCurrentToken);
            Toast.makeText(AmbientAuthActivity.this, "AuthToken\n" + mCurrentToken
                    + "\nInvalidated", LENGTH_SHORT).show();
        } else {
            Toast.makeText(AmbientAuthActivity.this, "No AuthToken", LENGTH_SHORT).show();
        }
    }

    public void chooseAccount() {
        Intent intent = AuthenticationServices.AuthenticationApi.newChooseAccountIntent();
        startActivityForResult(intent, REQUEST_CHOOSE_ACCOUNT);
    }
}
