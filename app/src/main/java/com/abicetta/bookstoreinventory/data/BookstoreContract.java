package com.abicetta.bookstoreinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookstoreContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private BookstoreContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.abicetta.bookstoreinventory";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.abicetta.bookstoreinventory.books is a valid path for
     * looking at bookstore data.
     * * Books database has two tables: com.abicetta.bookstoreinventory.books and suppliers.
     */
    public static final String PATH_BOOKS = "com.abicetta.bookstoreinventory.books";
    public static final String PATH_SUPPLIERS = "suppliers";

    /**
     * Inner class that defines constant values for the com.abicetta.bookstoreinventory.books database table.
     * Each entry in the table represents a single book.
     * * Books database has two tables: com.abicetta.bookstoreinventory.books and suppliers. BookEntry is for table com.abicetta.bookstoreinventory.books
     */
    public final static class BookEntry implements BaseColumns {
        /**
         * The content URI to access the book data in the provider
         */
        public static final Uri CONTENT_URI_BOOK = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);
        public final static String TABLE_NAME = "books";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_BOOK_TITLE = "title";
        public final static String COLUMN_BOOK_AUTHOR = "author";
        public final static String COLUMN_COD_ISBN = "isbn";
        public final static String COLUMN_BOOK_PRICE = "price";
        public final static String COLUMN_BOOK_QUANTITY = "quantity";
        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";
        public final static String COLUMN_SUPPLIER_PHONE = "supplier_phone";
        /**
         * The MIME type of the {@link #CONTENT_URI_BOOK} for a list of com.abicetta.bookstoreinventory.books
         */
        public static final String CONTENT_LIST_BOOK_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        /**
         * The MIME type of the {@link #CONTENT_URI_BOOK} for a single book.
         */
        public static final String CONTENT_ITEM_BOOK_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
    }

    /**
     * Inner class that defines constant values for the com.abicetta.bookstoreinventory.books database table.
     * Each entry in the table represents a single book.
     * * Books database has two tables: com.abicetta.bookstoreinventory.books and suppliers. SupplierEntry is for table suppliers
     */
    public final static class SupplierEntry implements BaseColumns {
        /**
         * The content URI to access the supplier data in the provider
         */
        public static final Uri CONTENT_URI_SUP = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SUPPLIERS);
        public final static String TABLE_NAME = "suppliers";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME_SUPPLIER = "name";
        public final static String COLUMN_PHONE_SUPPLIER = "phone";
        /**
         * The MIME type of the {@link #CONTENT_URI_SUP} for a list of suppliers.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLIERS;
        /**
         * The MIME type of the {@link #CONTENT_URI_SUP} for a single supplier.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLIERS;
    }
}