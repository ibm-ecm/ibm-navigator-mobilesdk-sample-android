/*
 * Licensed Materials - Property of IBM
 * (C) Copyright IBM Corporation 2015, 2023. All Rights Reserved.
 * This sample program is provided AS IS and may be used, executed, copied
 * and modified without royalty payment by customer (a) for its own instruction
 * and study, (b) in order to develop applications designed to run with an IBM
 * product, either for customer's own internal use or for redistribution by
 * customer, as part of such an application, in customer's own products.
 */
package app.sample.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import android.view.View;

import com.ibm.ecm.api.coresdk.exception.IBMECMRuntimeException;
import com.ibm.ecm.api.coresdk.factory.IBMECMFactory;
import com.ibm.ecm.api.coresdk.model.IBMECMApplication;
import com.ibm.ecm.api.coresdk.model.IBMECMCompletionHandler;
import com.ibm.ecm.api.coresdk.model.IBMECMDesktop;
import app.sample.R;
import app.sample.app.INMAndroidSDKSample;

/**
 Important: This sample project is not intended to be a tutorial on how to design or create an Android application.
 It is only for the purpose of showing how to use the IBM Content Navigator SDK.
 For example, it does not contain robust error handling, recovery, or ideal design structure.

 The initial login activity that is displayed to prompt the user for login information such as username, password,
 and IBM Content Navigator server's URL.
 */
public class SampleLoginActivity extends AppCompatActivity {

    private AppCompatEditText serverField;
    private AppCompatEditText userField;
    private AppCompatEditText passwordField;
    private AppCompatButton loginButton;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_login);

        this.userField = (AppCompatEditText) findViewById(R.id.username);
        this.passwordField = (AppCompatEditText) findViewById(R.id.password);
        this.serverField = (AppCompatEditText) findViewById(R.id.server);
        this.loginButton = (AppCompatButton) findViewById(R.id.loginButton);

        progressDialog = createProgressDialog(getResources().getString(R.string.loginProgress));

        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login(){
        String user = userField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String serverURL = serverField.getText().toString().trim();

        if (user == null || user.isEmpty() || password == null || password.isEmpty() || serverURL == null || serverURL.isEmpty())
            return;

        progressDialog.show();

        //The first initial step in using the SDK is to always obtain an IBMECMApplication object from the factory.
        //The application object can be obtained using the provided IBM Content Navigator URL.
        //Once an application is obtained once, you can re-obtain it with IBMECMFactory.getInstance().getCurrentApplication().
        final IBMECMApplication application = IBMECMFactory.getInstance().getApplication(serverURL);

        //Perform a login into the application with a username and password.
        //This login is performed against the current desktop and default repository set from the IBM Content Navigator's server URL.
        application.login(user, password, new IBMECMCompletionHandler<Boolean>() {
            @Override
            public void onCompleted(Boolean aBoolean) {
                loadDesktop(application);
            }

            @Override
            public void onError(IBMECMRuntimeException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }
        });
    }

    /**
     * Loads the current desktop set for this application.
     * @param application
     */
    public void loadDesktop(final IBMECMApplication application){
        //Once a login is performed, you can load a desktop using a desktopId or using the current desktop.
        //This call retrieves and loads all the desktop information you will need to access desktops and repositories.
        application.loadCurrentDesktop(new IBMECMCompletionHandler<IBMECMDesktop>() {
            @Override
            public void onCompleted(IBMECMDesktop ibmecmDesktop) {
                progressDialog.dismiss();

                INMAndroidSDKSample sampleApplication = (INMAndroidSDKSample) getApplicationContext();
                sampleApplication.setApplication(application);
                sampleApplication.setDesktop(ibmecmDesktop);
                sampleApplication.setRepository(ibmecmDesktop.getDefaultRepository());

                //You can start browsing or searching once a repository is obtained
                Intent intent = new Intent(SampleLoginActivity.this, SampleRootActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onError(IBMECMRuntimeException e) {
                e.printStackTrace();
            }
        });
    }

    private ProgressDialog createProgressDialog(String text) {
        ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(text);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        return mProgressDialog;
    }
}
