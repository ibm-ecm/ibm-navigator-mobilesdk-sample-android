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

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ibm.ecm.api.coresdk.exception.IBMECMRuntimeException;
import com.ibm.ecm.api.coresdk.factory.IBMECMFactory;
import com.ibm.ecm.api.coresdk.model.IBMECMRepository;
import app.sample.R;
import app.sample.app.INMAndroidSDKSample;
import app.sample.handler.UICompletionHandler;

/**
 Important: This sample project is not intended to be a tutorial on how to design or create an Android application.
 It is only for the purpose of showing how to use the IBM Content Navigator SDK.
 For example, it does not contain robust error handling, recovery, or ideal design structure.
 */
public class SampleRootActivity extends AppCompatActivity {

    private final static int MENU_ITEM_LOGOUT = Menu.FIRST;

    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        IBMECMRepository repository = ((INMAndroidSDKSample)getApplicationContext()).getRepository();
        getSupportActionBar().setTitle(repository.getName());

        listView = (ListView) findViewById(R.id.contentlist);
        String[] items = { getResources().getString(R.string.browse_repository), getResources().getString(R.string.search_repository)};
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    Intent intent = new Intent(SampleRootActivity.this, SampleBrowseActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(SampleRootActivity.this, SampleSearchActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_LOGOUT, 0, R.string.logout);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_ITEM_LOGOUT:
                logout();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Logs out of the application and ends the session.
     */
    private void logout(){
        //Obtains the current application being used and logs out
        IBMECMFactory.getInstance().getCurrentApplication().logoff(new UICompletionHandler<Boolean>() {
            @Override
            public void onCompletedUI(Boolean result) {
                Intent intent = new Intent(SampleRootActivity.this, SampleLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onErrorUI(IBMECMRuntimeException exp) {
                exp.printStackTrace();
            }
        });
    }
}
