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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.abicetta.bookstoreinventory.data.BookstoreContract;
import com.abicetta.bookstoreinventory.data.BookstoreContract.SupplierEntry;

/**
 * Displays list of suppliers of com.abicetta.bookstoreinventory.Books that were entered and stored in the app.
 */
public class SuppliersListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the supplier data loader
     */
    private static final int SUPPLIER_LOADER = 0;
    /**
     * Adapter for the ListView
     */
    SupplierCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suppliers_list);
        invalidateOptionsMenu();
        //set up FAB  to open SupplierEditorActivity
        FloatingActionButton insertItem = (FloatingActionButton) findViewById(R.id.insert);
        insertItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SuppliersListActivity.this, SupplierEditorActivity.class);
                startActivity(intent);
            }
        });

        //find the listView which will be populated with the suppliers data
        ListView suppliersListView = (ListView) findViewById(R.id.list);
        //find and set empty view on the listView, so that it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        suppliersListView.setEmptyView(emptyView);
        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new SupplierCursorAdapter(this, null);
        suppliersListView.setAdapter(mCursorAdapter);
        //Setup item click listener
        suppliersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intentEditSupplier = new Intent(SuppliersListActivity.this, SupplierEditorActivity.class);
                //Form the content URI that represent the specific supplier that was clicked on,
                // by appending the "id" (passed as input to this method) onto the {@link SupplierEntry#CONTENT_URI}
                // for example the URI would be "content://com.abicetta.bookstoreinventory/suppliers/2" if the supplier
                //with ID 2 was clicked on.
                Uri currentSupplierUri = ContentUris.withAppendedId(SupplierEntry.CONTENT_URI_SUP, id);
                //set the URI on the data field of the intent
                intentEditSupplier.setData(currentSupplierUri);
                //Launch the {@link EditorActivity} to display the data for current supplier
                startActivity(intentEditSupplier);
            }
        });
        //kick off (dare il via a) the loader
        getLoaderManager().initLoader(SUPPLIER_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return true;
    }

    /**
     * Helper method to insert dummy data into suppliers.db For debugging purposes only.
     */
    private void insertSupplier() {
        // Create a new map of values.
        ContentValues values = new ContentValues();
        values.put(SupplierEntry.COLUMN_NAME_SUPPLIER, "Peter Carter Editions");
        values.put(SupplierEntry.COLUMN_PHONE_SUPPLIER, "010 234567");
        // Receive the new content URI that will allow us to access "Peter Carter Editions" supplier's data in the future.
        Uri newUri = getContentResolver().insert(SupplierEntry.CONTENT_URI_SUP, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertSupplier();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.delete_all:
                showDeleteConfirmationDialog();
                return true;
            case R.id.add_supplier:
                Intent intentEditSupplier = new Intent(SuppliersListActivity.this, SupplierEditorActivity.class);
                startActivity(intentEditSupplier);
                return true;
            case R.id.add_books:
                Intent intentEditBook = new Intent(SuppliersListActivity.this, EditorActivity.class);
                startActivity(intentEditBook);
                return true;
            case android.R.id.home:
                Intent intentHome = new Intent(SuppliersListActivity.this, InventoryActivity.class);
                startActivity(intentHome);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * method to change the text of a menu item. ("Add a new supplier" instead of "Suppliers List")
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.add_supplier).setTitle(R.string.new_supplier);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Helper method to delete all suppliers in the database.
     */
    private void deleteAllSuppliers() {
        int rowsDeleted = getContentResolver().delete(SupplierEntry.CONTENT_URI_SUP, null, null);
        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_supplier_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_supplier_successful),
                    Toast.LENGTH_SHORT).show();
            getContentResolver().notifyChange(SupplierEntry.CONTENT_URI_SUP, null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from the table we care about.
        String[] vprojection = {
                SupplierEntry._ID,
                SupplierEntry.COLUMN_NAME_SUPPLIER,
                SupplierEntry.COLUMN_PHONE_SUPPLIER};
        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                SupplierEntry.CONTENT_URI_SUP,
                vprojection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update (@link BookCursorAdapter) with this new cursor containing updated supplier data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }

    /**
     * Prompt the user to confirm that they want to delete this supplier.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_suppliers_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the supplier.
                deleteAllSuppliers();
                updateAllBooks();
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

    private void updateAllBooks() {
        String supName = "Unknown";
        String supPhone = "Unknown";
        ContentValues values = new ContentValues();
        values.put(BookstoreContract.BookEntry.COLUMN_SUPPLIER_NAME, supName);
        values.put(BookstoreContract.BookEntry.COLUMN_SUPPLIER_PHONE, supPhone);
        // Receive the new content URI that will allow us to access  book's data in the future.
        int rowsUpdated = getContentResolver().update(BookstoreContract.BookEntry.CONTENT_URI_BOOK, values, null, null);
        // Show a toast message depending on whether or not the delete was successful.
        if (rowsUpdated == 0) {
            // If no rows were updated, then there was an error with the update.
            Toast.makeText(this, getString(R.string.suppliers_in_books_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.suppliers_in_books_successful),
                    Toast.LENGTH_SHORT).show();
            getContentResolver().notifyChange(BookstoreContract.BookEntry.CONTENT_URI_BOOK, null);
        }
    }
}