package com.abicetta.bookstoreinventory.data;

        import android.content.ContentProvider;
        import android.content.ContentUris;
        import android.content.ContentValues;
        import android.content.UriMatcher;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.net.Uri;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.util.Log;
        import android.widget.Toast;

        import com.abicetta.bookstoreinventory.R;
        import com.abicetta.bookstoreinventory.data.BookstoreContract.BookEntry;
        import com.abicetta.bookstoreinventory.data.BookstoreContract.SupplierEntry;

/**
 * {@link ContentProvider} for Bookstore app.
 */
public class BookProvider extends ContentProvider {
    /**
     * Database helper object
     */
    private BookstoreDbHelper mDbHelper;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = BookProvider.class.getSimpleName();


    /**
     * URI matcher code for the content URI for the com.abicetta.bookstoreinventory.books table
     */
    private static final int BOOKS = 100;

    /**
     * URI matcher code for the content URI for a single book in the com.abicetta.bookstoreinventory.books table
     */
    private static final int BOOK_ID = 101;

    /**
     * URI matcher code for the content URI for the suppliers table
     */
    private static final int SUPPLIERS = 200;

    /**
     * URI matcher code for the content URI for a single supplier in the suppliers table
     */
    private static final int SUPPLIER_ID = 201;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        // The content URI of the form "content://com.abicetta.bookstoreinventory.books" will map to the
        // integer code {@link #BOOKS}. This URI is used to provide access to MULTIPLE rows
        // of the com.abicetta.bookstoreinventory.books table.
        sUriMatcher.addURI(BookstoreContract.CONTENT_AUTHORITY, BookstoreContract.PATH_BOOKS, BOOKS);
        // The content URI of the form "content://com.abicetta.bookstoreinventory/.books/#" will map to the
        // integer code {@link #BOOK_ID}. This URI is used to provide access to ONE single row
        // of the com.abicetta.bookstoreinventory.books table.
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.abicetta.bookstoreinventory.books/3" matches, but
        // "content://com.abicetta.bookstoreinventory.books" (without a number at the end) doesn't match.
        sUriMatcher.addURI(BookstoreContract.CONTENT_AUTHORITY, BookstoreContract.PATH_BOOKS + "/#", BOOK_ID);
        //This URI is used to provide access to MULTIPLE rows of the suppliers table.
        sUriMatcher.addURI(BookstoreContract.CONTENT_AUTHORITY, BookstoreContract.PATH_SUPPLIERS, SUPPLIERS);
        //This URI is used to provide access to ONE single row of the suppliers table.
        sUriMatcher.addURI(BookstoreContract.CONTENT_AUTHORITY, BookstoreContract.PATH_SUPPLIERS + "/#", SUPPLIER_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        //Create and initialize a BookstoreDbHelper object to gain access to the com.abicetta.bookstoreinventory.books database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new BookstoreDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // For the BOOKS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the com.abicetta.bookstoreinventory.books table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.abicetta.bookstoreinventory.books/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the com.abicetta.bookstoreinventory.books table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SUPPLIERS:
                // For the SUPPLIERS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the suppliers table.
                cursor = database.query(SupplierEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SUPPLIER_ID:
                // For the SUPPLIER_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.abicetta.bookstoreinventory/suppliers/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = SupplierEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query on the suppliers table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(SupplierEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //Set notification URI on the Cursor, so we know what content was created for.
        //If the data at this URI  changes, then we know we need to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_BOOK_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_BOOK_TYPE;
            case SUPPLIERS:
                return SupplierEntry.CONTENT_LIST_TYPE;
            case SUPPLIER_ID:
                return SupplierEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            case SUPPLIERS:
                return insertSupplier(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a book into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBook(Uri uri, ContentValues values) {
        // Check that the book's title is not null
        String title = values.getAsString(BookEntry.COLUMN_BOOK_TITLE);
        if (title == null) {
            Toast.makeText(getContext(), R.string.null_title, Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("a title is required");
        }

        // Check that the book's author is not null
        String author = values.getAsString(BookEntry.COLUMN_BOOK_AUTHOR);
        if (author == null) {
            Toast.makeText(getContext(), "Author requires a name", Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Author requires a name");
        }

        // Check that the book's isbn is not null
        String isbn = values.getAsString(BookEntry.COLUMN_COD_ISBN);
        if (isbn.equals("")) {
            values.put(BookEntry.COLUMN_COD_ISBN, "Unknown");
        } else if (!isbn.matches("[0-9]+")) {
            Log.v(LOG_TAG, "Isbn is: " + isbn);
            Toast.makeText(getContext(), "ISBN requires a valid code", Toast.LENGTH_SHORT).show();

        } else if (isbn.length() != 10 && isbn.length() != 13) {
            Toast.makeText(getContext(), "ISBN code has 10 or 13 char numbers", Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("ISBN code has 10 or 13 char numbers");
        }

        // Check if the price is provided, that it's greater than or equal to 0
        Integer price = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
        Log.v("BOOKPROVIDER", "(insertBook) Integer price is: " + price);
        if (price != null && price < 0) {
            Toast.makeText(getContext(), "Book requires valid price", Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Book requires valid price");
        }

        // Check that the quantity is provided, that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
        if (quantity != null && quantity < 0) {
            Toast.makeText(getContext(), "Book requires valid quantity", Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Book requires valid quantity");
        }

        //Insert a new book into the com.abicetta.bookstoreinventory.books database table with the given ContentValues
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new pet with the given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the book content URI
        // uri: content://com.abicetta.bookstoreinventory.books
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Insert a supplier into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertSupplier(Uri uri, ContentValues values) {
        // Check that the supplier's name is not null
        String name = values.getAsString(SupplierEntry.COLUMN_NAME_SUPPLIER);
        if (name == null) {
            throw new IllegalArgumentException("the supplier's name is required");
        }

        // Check that the supplier's phone is not null
        String phone = values.getAsString(SupplierEntry.COLUMN_PHONE_SUPPLIER);
        if (phone == null) {
            throw new IllegalArgumentException("a valid phone number is required");
        }


        //Insert a new supplier into the suppliers database table with the given ContentValues
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new supplier with the given values
        long id = database.insert(SupplierEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the supplier content URI
        // uri: content://com.abicetta.bookstoreinventory/suppliers
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[]
            selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                return database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUPPLIERS:
                // Delete all rows that match the selection and selection args
                return database.delete(SupplierEntry.TABLE_NAME, selection, selectionArgs);
            case SUPPLIER_ID:
                // Delete a single row given by the ID in the URI
                selection = SupplierEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SupplierEntry.TABLE_NAME, selection, selectionArgs);
                break;

            //return database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            case SUPPLIERS:
                return updateSupplier(uri, values, selection, selectionArgs);
            case SUPPLIER_ID:
                // For the SUPPLIER_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = SupplierEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateSupplier(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update com.abicetta.bookstoreinventory.books in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more com.abicetta.bookstoreinventory.books).
     * Return the number of rows that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {

        // If the {@link BookEntry#COLUMN_BOOK_TITLE} key is present,
        // check that the name value is not null.
        if (values.containsKey(BookEntry.COLUMN_BOOK_TITLE)) {
            String name = values.getAsString(BookEntry.COLUMN_BOOK_TITLE);
            if (name == null) {
                throw new IllegalArgumentException("Book requires a title");
            }
        }
        // If the {@link BookEntry#COLUMN_BOOK_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            Integer price = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Book requires valid price");
            }
        }
        // If the {@link BookEntry#COLUMN_BOOK_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Book requires valid quantity");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Update suppliers in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more suppliers).
     * Return the number of rows that were successfully updated.
     */
    private int updateSupplier(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        // If the {@link SupplierEntry.COLUMN_NAME_SUPPLIER} key is present,
        // check that the name value is not null.
        if (values.containsKey(SupplierEntry.COLUMN_NAME_SUPPLIER)) {
            String name = values.getAsString(SupplierEntry.COLUMN_NAME_SUPPLIER);
            if (name == null) {
                throw new IllegalArgumentException("Supplier's name is required");
            }
        }
        // If the {@link SupplierEntry.COLUMN_PHONE_SUPPLIER} key is present,
        // check that the phone value is valid.
        if (values.containsKey(SupplierEntry.COLUMN_PHONE_SUPPLIER)) {
            String phone = values.getAsString(SupplierEntry.COLUMN_PHONE_SUPPLIER);
            if (phone == null) {
                throw new IllegalArgumentException("a valid phone number is required");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(SupplierEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }
}