package com.abicetta.bookstoreinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.abicetta.bookstoreinventory.data.BookstoreContract.SupplierEntry;
import com.abicetta.bookstoreinventory.data.BookstoreContract.BookEntry;

public class SupplierEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Content URI for the existing supplier to insert in the spinner (null if it's a new book)
     */
    private Uri mCurrentSupplierUri;

    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_SUPPLIER_LOADER = 0;

    /**
     * EditText field to enter the supplier's name in table suppliers
     */
    private EditText mSupplierEditText;

    /**
     * EditText field to enter the supplier's phone in table suppliers
     */
    private EditText mSupplierPhoneEditText;
    //variable to store the update value for book table
    private String nameToUpdate, nameUpdated, phoneUpdated;

    /**
     * Boolean flag that keeps track of whether the supplier has been edited (true) or not (false)
     */
    // this boolean variable will be true if the user updates part of the suppliers form.
    private boolean mSupplierHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mSupplierHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mSupplierHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_suppliers);
        // Find all relevant views that we will need to read user input from
        mSupplierEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);
        //set up FAB  to open EditorActivity
        FloatingActionButton insertSupplier = (FloatingActionButton) findViewById(R.id.save);
        insertSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save book to the database
                saveSupplier();
            }
        });

        //set up FAB  to delete supplier
        FloatingActionButton deleteSupplier = (FloatingActionButton) findViewById(R.id.delete);
        deleteSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteSupplierConfirmationDialog();
            }
        });
        //Examine the intent that was used to launch this fragment, in order to
        //figure out if we're creating a new supplier or editing an existing one
        Intent intent = getIntent();
        mCurrentSupplierUri = intent.getData();
        //If the intent DOES NOT contain a supplier content URI, then we know that we are creating a new supplier
        if (mCurrentSupplierUri == null) {
            //this is a new supplier, so change the app bar to say "Add a supplier"
            setTitle(R.string.editor_title_add_supplier);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a supplier that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            //otherwise this is an existing supplier, so change app bar to say "Edit Supplier"
            setTitle(R.string.editor_title_edit_supplier);

            // Initialize a loader to read the supplier data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_SUPPLIER_LOADER, null, this);
        }
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
    }

    /**
     * Get user input from editor and save new supplier into database.
     */
    private void saveSupplier() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mSupplierEditText.getText().toString().trim();
        if (nameString.equals("")) {
            Toast.makeText(this, R.string.supplier_name, Toast.LENGTH_LONG).show();
            return;
        }
        String phoneString = mSupplierPhoneEditText.getText().toString().trim();
        phoneString = phoneString.replaceAll("[^\\d]", "");
        if (phoneString.equals("")) {
            Toast.makeText(this, R.string.supplier_phone, Toast.LENGTH_LONG).show();
            return;
        } else if (!phoneString.matches("[0-9]+")) {
            Toast.makeText(this, R.string.valid_phone, Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a ContentValues object where column names are the keys,
        // and supplier attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(SupplierEntry.COLUMN_NAME_SUPPLIER, nameString);
        values.put(SupplierEntry.COLUMN_PHONE_SUPPLIER, phoneString);
        //store variable for updating books table
        nameUpdated = nameString;
        phoneUpdated = phoneString;
        changeSupplierInBooks(nameToUpdate, nameUpdated, phoneUpdated);
        // Determine if this is a new or existing supplier by checking if mCurrentSupplierUri is null or not
        if (mCurrentSupplierUri == null) {
            // This is a NEW supplier, so insert a new supplier into the provider,
            // returning the content URI for the new supplier.
            Uri newUri = getContentResolver().insert(SupplierEntry.CONTENT_URI_SUP, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(phoneString)) {
                // then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_supplier_failed),
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, getString(R.string.editor_insert_supplier_successful),
                        Toast.LENGTH_SHORT).show();
                resetEnterText();
            }
        } else {
            // Otherwise this is an EXISTING supplier, so update the supplier with content URI: mCurrent<supplierUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentSupplierUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = this.getContentResolver().update(mCurrentSupplierUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_supplier_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_supplier_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void resetEnterText() {
        mSupplierEditText.setText(null);
        mSupplierPhoneEditText.setText(null);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentSupplierUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save supplier to the database
                saveSupplier();
                //resetEnterText();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteSupplierConfirmationDialog();
                return true;
            case R.id.action_supplier_list:
                // Open SuppliersListActivity
                Intent intentList = new Intent(this, SuppliersListActivity.class);
                this.startActivity(intentList);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case R.id.action_home:
                // Navigate to InventoryActivity
                Intent intent = new Intent(this, SuppliersListActivity.class);
                this.startActivity(intent);
                // If the supplier hasn't changed, continue with navigating to
                // the {@link InventoryActivity}.
                if (!mSupplierHasChanged) {
                    NavUtils.navigateUpFromSameTask(SupplierEditorActivity.this);
                    // exit activity
                    finish();
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(SupplierEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the supplier editor shows all attributes of a supplier, define a projection that contains
        // all columns from the supplier table
        String[] projection = {
                SupplierEntry._ID,
                SupplierEntry.COLUMN_NAME_SUPPLIER,
                SupplierEntry.COLUMN_PHONE_SUPPLIER};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentSupplierUri,         // Query the content URI for the current supplier
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null); // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of supplier attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(SupplierEntry.COLUMN_NAME_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(SupplierEntry.COLUMN_PHONE_SUPPLIER);
            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            // Update the views on the screen with the values from the database
            mSupplierEditText.setText(name);
            mSupplierPhoneEditText.setText(phone);
            nameToUpdate = name;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mSupplierEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the editor.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this supplier.
     */
    private void showDeleteSupplierConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_supplier_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the supplier.
                nameUpdated = "Unknown";
                phoneUpdated = "Unknown";
                changeSupplierInBooks(nameToUpdate, nameUpdated, phoneUpdated);
                deleteSupplier();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the supplier.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteSupplier() {
        // Only perform the delete if this is an existing supplier.
        if (mCurrentSupplierUri != null) {
            // Call the ContentResolver to delete the supplier at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentSupplierUri
            // content URI already identifies the supplier that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentSupplierUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_supplier_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_supplier_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    private void changeSupplierInBooks(String nameToUpdate, String nameUpdated, String phoneUpdated) {
        ContentValues mUpdateValues = new ContentValues();
        mUpdateValues.put(BookEntry.COLUMN_SUPPLIER_NAME, nameUpdated);
        mUpdateValues.put(BookEntry.COLUMN_SUPPLIER_PHONE, phoneUpdated);
        // Defines a string to contain the selection clause
        String mSelectionClause = BookEntry.COLUMN_SUPPLIER_NAME + " = ?";
// Initializes an array to contain selection arguments
        String[] mSelectionArgs = {""};
        mSelectionArgs[0] = nameToUpdate;
// Defines a variable to contain the number of updated rows
        int mRowsUpdated = getContentResolver().update(
                BookEntry.CONTENT_URI_BOOK,
                mUpdateValues,
                mSelectionClause,
                mSelectionArgs
        );
    }
}