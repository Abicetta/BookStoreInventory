package com.abicetta.bookstoreinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.abicetta.bookstoreinventory.data.BookstoreContract.BookEntry;
import com.abicetta.bookstoreinventory.data.BookstoreContract.SupplierEntry;

public class BookstoreDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "com.abicetta.bookstoreinventory.books.db";
    //the com.abicetta.bookstoreinventory.books.db has two table: com.abicetta.bookstoreinventory.books and suppliers
    private static final String SQL_DELETE_BOOKS_ENTRIES =
            "DROP TABLE IF EXISTS " + BookstoreContract.BookEntry.TABLE_NAME;
    private static final String SQL_DELETE_SUPPLIERS_ENTRIES =
            "DROP TABLE IF EXISTS " + BookstoreContract.SupplierEntry.TABLE_NAME;

    /**
     * Constructs a new instance of {@link BookstoreDbHelper}.
     *
     * @param context of the app
     */
    public BookstoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     * Books database has two tables: com.abicetta.bookstoreinventory.books and suppliers.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Books table create Statements
        // Create a String that contains the SQL statement to create the com.abicetta.bookstoreinventory.books table
        String SQL_CREATE_BOOKS_ENTRIES = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_BOOK_TITLE + " TEXT NOT NULL, "
                + BookEntry.COLUMN_BOOK_AUTHOR + " TEXT NOT NULL, "
                + BookEntry.COLUMN_COD_ISBN + " TEXT, "
                + BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BookEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_SUPPLIER_PHONE + " TEXT);";
        db.execSQL(SQL_CREATE_BOOKS_ENTRIES);
        // Suppliers table create Statements
        // Create a String that contains the SQL statement to create the suppliers table
        String SQL_CREATE_SUPPLIERS_ENTRIES = "CREATE TABLE " + SupplierEntry.TABLE_NAME + " ("
                + SupplierEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SupplierEntry.COLUMN_NAME_SUPPLIER + " TEXT NOT NULL, "
                + SupplierEntry.COLUMN_PHONE_SUPPLIER + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_SUPPLIERS_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_BOOKS_ENTRIES);
        db.execSQL(SQL_DELETE_SUPPLIERS_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}