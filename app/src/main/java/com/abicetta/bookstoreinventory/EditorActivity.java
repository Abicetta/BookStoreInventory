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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abicetta.bookstoreinventory.data.BookstoreContract.BookEntry;
import com.abicetta.bookstoreinventory.data.BookstoreContract.SupplierEntry;

import java.util.ArrayList;

/**
 * Allows user to create a new book table or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    final ArrayList<Suppliers> suppliersList = new ArrayList<Suppliers>();
    final ArrayList<String> suppliersName = new ArrayList<String>();
    //integer variable for quantity
    int quantity;
    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;

    /**
     * EditText field to enter the book's title
     */
    private EditText mTitleEditText;

    /**
     * EditText field to enter the book's author
     */
    private EditText mAuthorEditText;

    /**
     * EditText field to enter the book's isbn
     */
    private EditText mIsbnEditText;

    /**
     * EditText field to enter the book's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the quantity
     */
    private EditText mQuantityEditText;

    /**
     * Spinner field to enter the supplier's name
     */
    private Spinner mSupplierSpinner;

    /**
     * Suppliers of the com.abicetta.bookstoreinventory.Books.
     */
    private String mSupplier = " ";
    private String mSupplierPhone = " ";
    // TextViews that show supplier's data of the selected book
    private TextView mSupName;
    private TextView mSupPhone;
    // variable that save price as int
    int price;

    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    // this boolean variable will be true if the user updates part of the book form.
    private boolean mBookHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mBookHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    // Handle the back button of the actionBar
    @Override
    public boolean onSupportNavigateUp() {
        // If the book hasn't changed, continue with navigating to
        // the {@link InventoryActivity}.
        if (!mBookHasChanged) {
            NavUtils.navigateUpFromSameTask(EditorActivity.this);
            // exit activity
            finish();
            return true;
        } else {
            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that
            // changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        }
                    };
            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        // Find all relevant views that we will need to read user input from
        mTitleEditText = (EditText) findViewById(R.id.your_book_title);
        mAuthorEditText = (EditText) findViewById(R.id.your_author);
        mIsbnEditText = (EditText) findViewById(R.id.isbn_code);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mSupplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);
        mSupName = (TextView) findViewById(R.id.supplier_selected_text);
        mSupPhone = (TextView) findViewById(R.id.supplier_selected_phone);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mQuantityEditText.setText("0");
        mPriceEditText.setText("0");
        //Examine the intent that was used in order to
        //figure out (capire, scoprire, decidere) if we're creating a new book or editing an existing one
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();
        //If the intent DOES NOT contain a book content URI, then we know that we are creating a new book
        if (mCurrentBookUri == null) {
            //this is a new book, so change the app bar to say "Add a book"
            setTitle(R.string.editor_title_add_book);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            //otherwise this is an existing book, so change app bar to say "Edit Book"
            setTitle(R.string.editor_title_edit_book);
            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mTitleEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mIsbnEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);
        setupSpinner();
        Button callToOrder = (Button) findViewById(R.id.button_order);
        callToOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = mSupPhone.getText().toString().trim();
                if (phone.contentEquals("Unknown")) {
                    Toast.makeText(v.getContext(), v.getContext().getString(R.string.phone_unknown), Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                    startActivity(intent);
                }
            }
        });

        ImageView plusIcon = (ImageView) findViewById(R.id.plus_button);
        plusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                quantity = quantity + 1;
                mBookHasChanged = true;
                mQuantityEditText.setText(Integer.toString(quantity));
                if (quantity <= 3) {
                    Toast.makeText(v.getContext(), v.getContext().getString(R.string.quantity_few), Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageView minusIcon = (ImageView) findViewById(R.id.minus_button);
        minusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                if (quantity < 0) {
                    Toast.makeText(v.getContext(), v.getContext().getString(R.string.quantity_zero), Toast.LENGTH_LONG).show();
                } else {
                    quantity = quantity - 1;
                    mBookHasChanged = true;
                    mQuantityEditText.setText(Integer.toString(quantity));
                    if (quantity <= 3) {
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.quantity_few), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //set up FAB  to open EditorActivity
        FloatingActionButton insertItem = (FloatingActionButton) findViewById(R.id.save);
        insertItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save book to the database
                saveBook();
            }
        });

        //set up FAB  to open EditorActivity
        FloatingActionButton deleteItem = (FloatingActionButton) findViewById(R.id.delete);
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        //set up add a new supplier  to open SupplierEditorActivity
        Button addSupplier = (Button) findViewById(R.id.text_new_supplier);
        addSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditorActivity.this, SupplierEditorActivity.class);
                startActivity(intent);
            }
        });
        //set up check isbn button  to open CheckDataActivity
        Button checkData = (Button) findViewById(R.id.check_isbn);
        checkData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCheck = new Intent(EditorActivity.this, CheckDataActivity.class);
                if (mCurrentBookUri == null) {
                    Toast.makeText(EditorActivity.this, "Tou have to insert and save data before check them", Toast.LENGTH_SHORT).show();
                } else {
                    intentCheck.setData(mCurrentBookUri);
                    //Launch the {@link CheckDataActivity} to display the data for current book
                    startActivity(intentCheck);
                }
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier of the book.
     */
    private void setupSpinner() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                SupplierEntry.COLUMN_NAME_SUPPLIER,
                SupplierEntry.COLUMN_PHONE_SUPPLIER};
        Cursor cursor = getContentResolver().query(SupplierEntry.CONTENT_URI_SUP, projection, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    Suppliers singleSupplier = new Suppliers();
                    singleSupplier.setName(cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_NAME_SUPPLIER)));
                    suppliersName.add(cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_NAME_SUPPLIER)));
                    singleSupplier.setPhone(cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_PHONE_SUPPLIER)));
                    suppliersList.add(singleSupplier);
                } while (cursor.moveToNext());
            }
            //create an adapter from the arraylist
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_dropdown_item, suppliersName);
            // Apply the adapter to the spinner
            mSupplierSpinner.setAdapter(adapter);//return supplierList
        } finally {
            cursor.close();
        }
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                int numberOfItems = suppliersName.size();
                if (!TextUtils.isEmpty(selection)) {
                    for (int j = 0; j < numberOfItems; j++) {
                        if (selection.equals(suppliersList.get(j).getName())) {
                            mSupplier = suppliersList.get(j).getName();
                            mSupplierPhone = suppliersList.get(j).getPhone();
                        }
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = "Unknown"; // Unknown
                mSupplierPhone = "Unknown"; // Unknown
            }
        });
    }

    /**
     * Get user input from editor and save new book into database.
     */
    private void saveBook() {
        mBookHasChanged = false;
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String titleString = mTitleEditText.getText().toString().trim();
        if (titleString.equals("")) {
            Toast.makeText(this, R.string.title_unknown, Toast.LENGTH_LONG).show();
            return;
        }

        String authorString = mAuthorEditText.getText().toString().trim();
        if (authorString.equals("")) {
            Toast.makeText(this, R.string.author_unknown, Toast.LENGTH_LONG).show();
            return;
        }

        String isbnString = mIsbnEditText.getText().toString().trim();
        if (isbnString.equals("") || isbnString.equals(R.string.unknown)) {
            Toast.makeText(this, R.string.isbn_null + isbnString, Toast.LENGTH_LONG).show();
            // isbnString = "Unknown";
        } else if (!isbnString.matches("[0-9]+")) {
            Toast.makeText(this, R.string.isbn_valid, Toast.LENGTH_SHORT).show();
            return;
        } else if (isbnString.length() != 10 && isbnString.length() != 13) {
            Toast.makeText(this, R.string.isbn_numbers, Toast.LENGTH_SHORT).show();
            return;
        }

        String priceString = mPriceEditText.getText().toString().trim();
        double mPrice = 0.00;
        if (!priceString.equals("")) {
            mPrice = Double.parseDouble(priceString);
        }
        mPrice = mPrice * 100.00;
        price = (int) mPrice;
        if (price < 0) {
            Toast.makeText(this, "Book requires valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        String quantityString = mQuantityEditText.getText().toString().trim();
        //Integer.parseInt("1") -> 1
        int quantity = !quantityString.equals("") ? Integer.parseInt(quantityString) : 0;
        if (quantity < 0) {
            Toast.makeText(this, R.string.quantity_valid, Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a ContentValues object where column names are the keys,
        // and com.abicetta.bookstoreinventory.books attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_TITLE, titleString);
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, authorString);
        values.put(BookEntry.COLUMN_COD_ISBN, isbnString);
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, mSupplier);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, mSupplierPhone);
        // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
        if (mCurrentBookUri == null) {
            // This is a NEW book, so insert a new book into the provider,
            // returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI_BOOK, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null && TextUtils.isEmpty(titleString) && TextUtils.isEmpty(authorString)
                    && TextUtils.isEmpty(isbnString) && TextUtils.isEmpty(priceString)
                    && TextUtils.isEmpty(quantityString)) {
                // then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
                resetEnterText();
                finish();
            }
        } else {
            // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentBookUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = this.getContentResolver().update(mCurrentBookUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void resetEnterText() {
        mTitleEditText.setText(null);
        mAuthorEditText.setText(null);
        mIsbnEditText.setText(null);
        mPriceEditText.setText(null);
        mQuantityEditText.setText(null);
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
        if (mCurrentBookUri == null) {
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
                // Save book to the database
                saveBook();
                //resetEnterText();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_supplier_list:
                // Open SuppliersListActivity
                Intent intent = new Intent(this, SuppliersListActivity.class);
                this.startActivity(intent);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case R.id.action_home:
                // If the book hasn't changed, continue with navigating to
                // the {@link InventoryActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
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
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
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
        // Since the editor shows all attributes of a book, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_TITLE,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_COD_ISBN,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE};
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
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_TITLE);
            int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
            int isbnColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_COD_ISBN);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);
            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String isbn = cursor.getString(isbnColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            double mPrice = (double) price / 100.00;
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            // Update the views on the screen with the values from the database
            mTitleEditText.setText(title);
            mAuthorEditText.setText(author);
            mIsbnEditText.setText(isbn);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Double.toString(mPrice));
            mSupName.setText(supplierName);
            mSupPhone.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitleEditText.setText("");
        mAuthorEditText.setText("");
        mQuantityEditText.setText("");
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
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
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
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
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
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}