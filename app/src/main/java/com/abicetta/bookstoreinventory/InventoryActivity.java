package com.abicetta.bookstoreinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.abicetta.bookstoreinventory.data.BookstoreContract.BookEntry;

/**
 * Displays list of com.abicetta.bookstoreinventory.Books that were entered and stored in the app.
 */
public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the book data loader
     */
    private static final int BOOK_LOADER = 0;
    /**
     * Adapter for the ListView
     */
    BookCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //set up FAB  to open EditorActivity
        FloatingActionButton insertItem = (FloatingActionButton) findViewById(R.id.insert);
        insertItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        //find the listView which will be populated with the book data
        ListView bookListView = (ListView) findViewById(R.id.list);
        //find and set empty view on the listView, so that it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);
        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);
        //Setup item click listener
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intentEditBook = new Intent(InventoryActivity.this, EditorActivity.class);
                //Form the content URI that represent the specific book that was clicked on,
                // by appending the "id" (passed as input to this method) onto the {@link BookEntry#CONTENT_URI}
                // for example the URI would be "content://com.abicetta.bookstoreinventory/com.books/2" if the book
                //with ID 2 was clicked on.
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI_BOOK, id);
                //set the URI on the data field of the intent
                intentEditBook.setData(currentBookUri);
                //Launch the {@link EditorActivity} to display the data for current book
                startActivity(intentEditBook);
            }
        });
        //kick off (dare il via a) the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return true;
    }

    /**
     * Helper method to insert dummy data into com.abicetta.bookstoreinventory.Books.db For debugging purposes only.
     */
    private void insertBook() {
        // Create a ContentValues object.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_TITLE, "Android Programming");
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, "Bill Phillips");
        values.put(BookEntry.COLUMN_COD_ISBN, "9780134706054");
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, "10");
        values.put(BookEntry.COLUMN_BOOK_PRICE, "4999");
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, "Gigi store");
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, "001 0123456");
        // Receive the new content URI that will allow us to access "Android Programming" book's data in the future.
        Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI_BOOK, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.delete_all:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Add a supplier" menu option
            case R.id.add_supplier:
                Intent intentEditSupplier = new Intent(InventoryActivity.this, SuppliersListActivity.class);
                startActivity(intentEditSupplier);
                return true;
            // Respond to a click on the "Add a new book" menu option
            case R.id.add_books:
                Intent intentEditBook = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intentEditBook);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to delete all com.abicetta.bookstoreinventory.Books in the database.
     */
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI_BOOK, null, null);
        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                    Toast.LENGTH_SHORT).show();
            getContentResolver().notifyChange(BookEntry.CONTENT_URI_BOOK, null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from the table we care about.
        String[] vprojection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_TITLE,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_COD_ISBN,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE};
        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                BookEntry.CONTENT_URI_BOOK,
                vprojection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update (@link BookCursorAdapter) with this new cursor containing updated book data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the com.abicetta.bookstoreinventory.Books.
                deleteAllBooks();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}