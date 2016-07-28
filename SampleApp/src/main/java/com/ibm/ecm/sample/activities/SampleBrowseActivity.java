package com.ibm.ecm.sample.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ibm.ecm.api.coresdk.collection.IBMECMItemProperties;
import com.ibm.ecm.api.coresdk.collection.IBMECMResultSet;
import com.ibm.ecm.api.coresdk.constants.IBMECMContentSourceType;
import com.ibm.ecm.api.coresdk.exception.IBMECMRuntimeException;
import com.ibm.ecm.api.coresdk.factory.IBMECMFactory;
import com.ibm.ecm.api.coresdk.model.IBMECMContentItem;
import com.ibm.ecm.api.coresdk.model.IBMECMProgressListener;
import com.ibm.ecm.api.coresdk.model.IBMECMRepository;
import com.ibm.ecm.sample.R;
import com.ibm.ecm.sample.adapter.ContentItemArrayAdapter;
import com.ibm.ecm.sample.app.IBMECMSampleApplication;
import com.ibm.ecm.sample.handler.UICompletionHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 Important: This sample project is not intended to be a tutorial on how to design or create an Android application.
 It is only for the purpose of showing how to use the IBM Content Navigator SDK.
 For example, it does not contain robust error handling, recovery, or ideal design structure.

 The main browsing activity for a given repository displaying folders and documents. For each folder, you can open that folder and see
 additional content.  For documents, you will be able to view the document's content.

 You can also add a new folder and document as well.
 */
public class SampleBrowseActivity extends AppCompatActivity {

    private final static int MENU_ITEM_ADD_DOC = Menu.FIRST;
    private final static int MENU_ITEM_ADD_FOLDER = Menu.FIRST + 1;

    private final static String CONTENT_ITEM = "contentItem";

    private ListView listView;
    private ArrayAdapter<IBMECMContentItem> adapter;
    private IBMECMResultSet<IBMECMContentItem> resultSet;

    private IBMECMContentItem contentItem;
    private IBMECMRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contentItem = (IBMECMContentItem) getIntent().getSerializableExtra(CONTENT_ITEM);

        //Obtain the repository that was saved previously from login.  Look at the SampleLoginActivity for more references.
        repository = ((IBMECMSampleApplication)getApplicationContext()).getRepository();

        listView = (ListView) findViewById(R.id.contentlist);
        adapter = new ContentItemArrayAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            onItemClicked(position);
            }
        });

        if (contentItem == null) {

            //You can obtain the root item in a repository and start browsing from that IBMECMContentItem.
            repository.retrieveRootItem(new UICompletionHandler<IBMECMContentItem>() {
                @Override
                public void onCompletedUI(IBMECMContentItem result) {
                    contentItem = result;
                    openFolder();
                }

                @Override
                public void onErrorUI(IBMECMRuntimeException exp) {
                    exp.printStackTrace();
                }
            });
        }
        else {
            openFolder();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        if (outState != null){
            outState.putSerializable(CONTENT_ITEM, contentItem);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null){
            contentItem = (IBMECMContentItem) savedInstanceState.getSerializable(CONTENT_ITEM);
            repository = ((IBMECMSampleApplication)getApplicationContext()).getRepository();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (contentItem != null && !contentItem.isRootFolder())
            getSupportActionBar().setTitle(contentItem.getName());
        else
            getSupportActionBar().setTitle(repository.getName());
    }

    /**
     * If an item is clicked, find the right content item associated with that position and either open it by starting another browse activity
     * or viewing the document item directly.
     *
     * @param position
     */
    public void onItemClicked(int position){
        final IBMECMContentItem item = adapter.getItem(position);

        if (item.isFolder()) {
            Intent intent = new Intent(this, SampleBrowseActivity.class);
            intent.putExtra(CONTENT_ITEM, item);
            startActivity(intent);
        } else {
            openDocument(item);
        }
    }

    /**
     * Opens a given document item by obtaining the file name.  If the file name is not available, retrieve all the attributes for the content item to find it.
     *
     * @param item
     */
    private void openDocument(final IBMECMContentItem item) {
        if (item.getFileName() != null){
            retrieveDocument(item.getFileName(), item);
        }
        else {
            //Retrieving attributes fetches all properties for a given content item. A content item opened from a folder is initialized with minimal properties for performance reasons.
            //If you need additional properties or attributes, make this call to fetch them individually.
            item.retrieveAttributes(new UICompletionHandler<IBMECMContentItem>() {
                @Override
                public void onCompletedUI(IBMECMContentItem result) {
                    retrieveDocument(result.getFileName(), result);
                }

                @Override
                public void onErrorUI(IBMECMRuntimeException exp) {
                    exp.printStackTrace();
                }
            });
        }
    }

    //Retrieves the actual content of a document item and passes the cached file content to a viewer.
    private void retrieveDocument(String filename, final IBMECMContentItem item){
        final File file = new File(getExternalCacheDir().getPath() + File.pathSeparator + item.getId() + File.pathSeparator + filename);
        file.deleteOnExit();

        if (file.exists()){
            viewFile(item, file);
        }
        else {
            //Fetches the actual content of the document item and streams it into a given file.
            // You can also listen to the progress and report back using the provided progress listener.
            item.retrieveDocumentItem(file, new UICompletionHandler<Boolean>() {
                @Override
                public void onCompletedUI(Boolean result) {
                    viewFile(item, file);
                }

                @Override
                public void onErrorUI(IBMECMRuntimeException exp) {
                    exp.printStackTrace();
                }
            }, new IBMECMProgressListener() {
                @Override
                public void onProgress(long l, long l1, long l2) {

                }
            });
        }
    }

    /**
     * Views a file by finding an appropriate application on the android phone.
     * @param item
     * @param file
     */
    private void viewFile(IBMECMContentItem item, File file){
        Intent i = new Intent();
        i.setAction(android.content.Intent.ACTION_VIEW);
        i.setDataAndType(Uri.fromFile(file), item.getMimetype());
        startActivity(i);
    }

    /**
     * Opens a folder by retrieving all the folder's content.
     */
    public void openFolder(){
        if (contentItem != null){
            //Opens a folder by retrieving the folder's content. The call returns a IBMECMResultSet of content items. You can then grab all the items or keep paging to grab initial items.
            //The call allows you to set whether to return folder only, set a different sort property, page size, and etc.
            contentItem.retrieveFolderContent(false, null, false, 25, null, new UICompletionHandler<IBMECMResultSet<IBMECMContentItem>>() {
                @Override
                public void onCompletedUI(IBMECMResultSet<IBMECMContentItem> result) {
                    resultSet = result;
                    adapter.addAll(result.getItems());
                }

                @Override
                public void onErrorUI(IBMECMRuntimeException exp) {
                    exp.printStackTrace();
                }
            });
        }
    }

    /**
     * Loads additional pages for this folder by checking to see if the result set has more and then calling the retrieveNextPage call.
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_ADD_DOC, 0, R.string.adddoc);
        menu.add(0, MENU_ITEM_ADD_FOLDER, 0, R.string.addfolder);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_ITEM_ADD_DOC:
                addDoc();
                break;
            case MENU_ITEM_ADD_FOLDER:
                addFolder();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds a new document via a hardcoded name, file path, and mimetype.
     */
    private void addDoc() {
        String parentFolderId = contentItem.getId();
        String classId = "Document";

        String docTitle = "document_" + Math.abs(new Random().nextInt()) + ".jpg";
        String fileName = "ibm.jpg";
        String mimeType = "jpeg";
        try {
            InputStream stream = getAssets().open(fileName);
            boolean addAsMinorVersion = false;

            //Obtains an empty properties collection and sets an appropriate property value for this particular document class.
            //The properties being added should match the attribute definitions for the class.
            //In this case, in the Document class, there is an attribute definition called "DocumentTitle" which is reserved for the document's name.
            IBMECMItemProperties properties = IBMECMFactory.getInstance().getIBMECMItemProperties();
            properties.add("DocumentTitle", docTitle);

            //Adds a new document for this repository by indicating where to add into (parentFolderId), what class to add as (classId), source type, properties, mimetype, filename, and etc.
            //The content of the document can be added as a file or a stream.
            //You can also listen the progress of the add using the progress listener.
            repository.addDocumentItem(parentFolderId, null, classId, IBMECMContentSourceType.DOCUMENT, properties, mimeType, fileName, stream, addAsMinorVersion, new UICompletionHandler<IBMECMContentItem>() {
                @Override
                public void onCompletedUI(IBMECMContentItem result) {
                    adapter.add(result);
                }

                @Override
                public void onErrorUI(IBMECMRuntimeException exp) {
                    exp.printStackTrace();
                }
            }, new IBMECMProgressListener() {
                @Override
                public void onProgress(long l, long l1, long l2) {

                }
            });
        }
        catch(IOException exp){
            exp.printStackTrace();
        }
    }

    /**
     * Adds a new folder item to this repository
     */
    private void addFolder() {
        String parentFolderId = contentItem.getId();
        String classId = "Folder";
        String folderName = "newFolder_" + Math.abs(new Random().nextInt());

        //Obtains an empty properties collection and sets an appropriate property value for this particular folder class.
        //The properties being added should match the attribute definitions for the class.
        //In this case, in the Folder class, there is an attribute definition called "FolderName" which is reserved for the folder's name.
        IBMECMItemProperties properties = IBMECMFactory.getInstance().getIBMECMItemProperties();
        properties.add("FolderName", folderName);

        //Adds a new folder item under a particular parent folder with a particular class.
        repository.addFolderItem(classId, parentFolderId, null, properties, new UICompletionHandler<IBMECMContentItem>() {
            @Override
            public void onCompletedUI(IBMECMContentItem result) {
                adapter.add(result);
            }

            @Override
            public void onErrorUI(IBMECMRuntimeException exp) {
                exp.printStackTrace();
            }
        });
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
