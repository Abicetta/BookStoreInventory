package com.abicetta.bookstoreinventory;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.app.LoaderManager;
import android.widget.Toast;

import com.abicetta.bookstoreinventory.data.BookstoreContract.BookEntry;

import org.json.JSONException;

import static com.abicetta.bookstoreinventory.QueryUtils.fetchBooksData;

public class CheckDataActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = CheckDataActivity.class.getName();
    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;
    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;
    private TextView titleText, authorText, isbnText, mEmptyStateTextView, titleBook, authorBook, titleView, authorView, textQuestion;
    private String isbnBook, changedTitle, changedAuthor;
    private View loadingIndicator;
    private Button yesButton, noButton, returnButton;

    /**
     * URL for articles from the guardianapis.com dataset
     */
    private final String GOOGLEAPIS_REQUEST_URL_BASE = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private String GOOGLEAPIS_REQUEST_URL = "";
    /**
     * Constant value for the article loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int DATA_LOADER_ID = 1;

    /**
     * TextView that is displayed when there is no data
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_isbn);
        mCurrentBookUri = getIntent().getData();
        titleText = (TextView) findViewById(R.id.your_book_title);
        authorText = (TextView) findViewById(R.id.your_author);
        isbnText = (TextView) findViewById(R.id.isbn_code);
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (activeNetwork != null && activeNetwork.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(DATA_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet);
        }
        //set up yesButton to load data from googleBooksApi
        yesButton = (Button) findViewById(R.id.replace);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckDataActivity.this, EditorActivity.class);
                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_BOOK_TITLE, changedTitle);
                values.put(BookEntry.COLUMN_BOOK_AUTHOR, changedAuthor);
                int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(CheckDataActivity.this, getString(R.string.editor_update_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(CheckDataActivity.this, getString(R.string.editor_update_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
                intent.setData(mCurrentBookUri);
                startActivity(intent);
            }
        });
        //set up noButton to skip data from googleBooksApi and return to InventoryActivity
        noButton = (Button) findViewById(R.id.no_replace);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckDataActivity.this, InventoryActivity.class);
                startActivity(intent);
            }
        });
        //set up returnButton to return to InventoryActivity
        returnButton = (Button) findViewById(R.id.return_inventory);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckDataActivity.this, InventoryActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all attributes of a book, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_TITLE,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_COD_ISBN};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null); // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        titleBook = (TextView) findViewById(R.id.book_title_isbn);
        authorBook = (TextView) findViewById(R.id.author_isbn);
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_TITLE);
            int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
            int isbnColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_COD_ISBN);
            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String isbn = cursor.getString(isbnColumnIndex);
            // Update the views on the screen with the values from the database
            titleText.setText(title);
            authorText.setText(author);
            isbnText.setText(isbn);
            isbnBook = isbn;
            cursor.close();
        }
        GOOGLEAPIS_REQUEST_URL = GOOGLEAPIS_REQUEST_URL_BASE + isbnBook;
        try {
            BooksOb thisBook = fetchBooksData(GOOGLEAPIS_REQUEST_URL);
            // Hide loading indicator because the data has been loaded
            loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            if (thisBook == null) {
                // Set empty state text to display "No articles found."
                mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
                mEmptyStateTextView.setText(R.string.no_data);
                //hide descriptive views and non-contextual buttons
                textQuestion = (TextView) findViewById(R.id.text_question);
                textQuestion.setVisibility(View.GONE);
                titleView = (TextView) findViewById(R.id.title_text_isbn);
                titleView.setVisibility(View.GONE);
                titleBook.setVisibility(View.GONE);
                authorView = (TextView) findViewById(R.id.author_text_isbn);
                authorView.setVisibility(View.GONE);
                authorBook.setVisibility(View.GONE);
                ViewGroup buttons = (ViewGroup) findViewById(R.id.yes_no_buttons);
                buttons.setVisibility(View.GONE);
            } else {
                //Hide empty state text and set descriptive views
                mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
                mEmptyStateTextView.setVisibility(View.GONE);
                titleBook.setText(thisBook.getBookTitle());
                changedTitle = thisBook.getBookTitle();
                authorBook.setText(thisBook.getBookAuthor());
                changedAuthor = thisBook.getBookAuthor();
                //hide non-contextual button
                returnButton = (Button) findViewById(R.id.return_inventory);
                returnButton.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        titleText.setText("");
        authorText.setText("");
        isbnText.setText("");
    }
}