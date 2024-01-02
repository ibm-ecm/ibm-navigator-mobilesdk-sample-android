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

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ibm.ecm.api.coresdk.collection.IBMECMResultSet;
import com.ibm.ecm.api.coresdk.constants.IBMECMObjectType;
import com.ibm.ecm.api.coresdk.constants.IBMECMPropertyCardinality;
import com.ibm.ecm.api.coresdk.constants.IBMECMPropertyDataType;
import com.ibm.ecm.api.coresdk.exception.IBMECMRuntimeException;
import com.ibm.ecm.api.coresdk.factory.IBMECMFactory;
import com.ibm.ecm.api.coresdk.model.IBMECMCompletionHandler;
import com.ibm.ecm.api.coresdk.model.IBMECMContentItem;
import com.ibm.ecm.api.coresdk.model.IBMECMRepository;
import com.ibm.ecm.api.coresdk.model.IBMECMSearchPredicate;
import com.ibm.ecm.api.coresdk.model.IBMECMSearchPredicateBuilder;
import app.sample.R;
import app.sample.adapter.ContentItemArrayAdapter;
import app.sample.app.INMAndroidSDKSample;
import app.sample.handler.UICompletionHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 Important: This sample project is not intended to be a tutorial on how to design or create an Android application.
 It is only for the purpose of showing how to use the IBM Content Navigator SDK.
 For example, it does not contain robust error handling, recovery, or ideal design structure.

 Performs ad-hoc document searching for a given repository using the two main criteria set -- DocumentTitle and DateCreated

 You can also add a new folder and document as well.
 */
public class SampleSearchActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<IBMECMContentItem> adapter;
    private IBMECMResultSet<IBMECMContentItem> resultSet;

    private AppCompatButton searchButton;
    private AppCompatEditText documentTitleEditText;
    private AppCompatEditText dateCreatedEditText;
    private IBMECMContentItem searchFolder;

    private IBMECMRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Obtain the repository that was saved previously from login.  Look at the SampleLoginActivity for more references.
        repository = ((INMAndroidSDKSample)getApplicationContext()).getRepository();

        getSupportActionBar().setTitle(getResources().getString(R.string.search_repository));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.contentlist);
        adapter = new ContentItemArrayAdapter(this);
        listView.setAdapter(adapter);

        searchButton = (AppCompatButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performDocumentSearch();
            }
        });

        documentTitleEditText = (AppCompatEditText) findViewById(R.id.documentTitle);
        dateCreatedEditText = (AppCompatEditText) findViewById(R.id.dateCreated);
    }

    /**
     * Performs an ad hoc document search using search predicates like DocumentTitle and DateCreated.
     */
    private void performDocumentSearch() {
        hideKeyboard();

        String documentTitleValue = documentTitleEditText.getText().toString();
        String dateCreatedValue = dateCreatedEditText.getText().toString();

        //You can set whether to search for any particular classes like Document, Folder, or any other subclasses.
        final String[] searchClasses = new String[]{ "Document" };

        //Indicate which object type we are searching for
        final IBMECMObjectType objectType = IBMECMObjectType.DOCUMENT;

        //Obtain a search predicate builder to add any search predicates you need to search against.
        //The builder allows for likes, betweens, ins, and etc.
        IBMECMFactory factory = IBMECMFactory.getInstance();
        IBMECMSearchPredicateBuilder searchPredicateBuilder = factory.getIBMECMSearchPredicateBuilder();
        List<IBMECMSearchPredicate> searchPredicateList = new ArrayList<>();

        //Adds in the DocumentTitle search predicate
        if (documentTitleValue != null && !documentTitleValue.isEmpty())
            searchPredicateList.add(searchPredicateBuilder.Like("DocumentTitle", IBMECMPropertyDataType.STRING, IBMECMPropertyCardinality.SINGLE, new String[] { documentTitleValue }));

        //For dates, we need to parse the date entered by user and convert it to UTC format so that the server can understand
        if (dateCreatedValue != null && !dateCreatedValue.isEmpty()) {
            Date date = null;
            SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyyy");
            try {
                date = parser.parse(dateCreatedValue);
            }
            catch (ParseException exp){
                exp.printStackTrace();
            }

            if (date != null) {
                String timestamp = convertToUTCTime(date);
                searchPredicateList.add(searchPredicateBuilder.GreaterOrEqual("DateCreated", IBMECMPropertyDataType.TIMESTAMP, IBMECMPropertyCardinality.SINGLE, new String[]{ timestamp }));
            }
        }

        //Construct the final search predicates list
        final IBMECMSearchPredicate[] searchPredicates = searchPredicateList.toArray(new IBMECMSearchPredicate[searchPredicateList.size()]);

        final String orderBy = "DocumentTitle";
        final boolean isDesending = false;
        final int pageSize = 25;

        //Obtain the root folder that this search will be performed in.
        //Right now we are searching basically the entire repository since we are retrieving the root folder.
        if (searchFolder == null) {
            repository.retrieveRootItem(new IBMECMCompletionHandler<IBMECMContentItem>() {
                @Override
                public void onCompleted(IBMECMContentItem contentItem) {
                    searchFolder = contentItem;
                    search(searchClasses, objectType, searchPredicates, orderBy, isDesending, pageSize);
                }

                @Override
                public void onError(IBMECMRuntimeException e) {
                    e.printStackTrace();
                }
            });
        }
        else {
            search(searchClasses, objectType, searchPredicates, orderBy, isDesending, pageSize);
        }
    }

    //Performs the actual adhoc search by passing in all the parameters
    public void search(String[] searchClasses, IBMECMObjectType objectType, IBMECMSearchPredicate[] searchPredicates, String orderBy, boolean isDesending, int pageSize){
        repository.searchAdHoc(searchFolder.getId(), null, searchClasses, objectType, searchPredicates, null, orderBy, isDesending, pageSize, new UICompletionHandler<IBMECMResultSet<IBMECMContentItem>>() {
            @Override
            public void onCompletedUI(IBMECMResultSet<IBMECMContentItem> result) {
                resultSet = result;
                adapter.clear();
                adapter.addAll(result.getItems());
            }

            @Override
            public void onErrorUI(IBMECMRuntimeException exp) {
                exp.printStackTrace();
            }
        });
    }

    /**
     * Loads additional pages for this search by checking to see if the result set has more and then calling the retrieveNextPage call.
     */
    public void loadNextPage() {
        //A resultset contains the page size and all the items for that given page. The caller can keep checking to see if it has more pages and keep calling retrieveNextPage to retrieve all
        //items in a folder.
        if (resultSet.hasMore()) {
            resultSet.retrieveNextPage(new UICompletionHandler<IBMECMResultSet<IBMECMContentItem>>() {
                @Override
                public void onCompletedUI(IBMECMResultSet<IBMECMContentItem> result) {
                    adapter.addAll(result.getItems());
                }

                @Override
                public void onErrorUI(IBMECMRuntimeException exp) {
                    exp.printStackTrace();
                }
            });
        }
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private String convertToUTCTime(Date date){
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }
}
