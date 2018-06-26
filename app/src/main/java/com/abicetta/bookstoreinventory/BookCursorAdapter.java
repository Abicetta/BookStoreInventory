package com.abicetta.bookstoreinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.abicetta.bookstoreinventory.data.BookstoreContract;
import com.abicetta.bookstoreinventory.data.BookstoreContract.BookEntry;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
public class BookCursorAdapter extends CursorAdapter {

    TextView quantityTextView;
    int bookQuantity;

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the title for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView titleTextView = (TextView) view.findViewById(R.id.title);
        TextView authorTextView = (TextView) view.findViewById(R.id.author);
        quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView isbnTextView = (TextView) view.findViewById(R.id.isbn);
        TextView supNameTextView = (TextView) view.findViewById(R.id.supplier_name);
        TextView supPhoneTextView = (TextView) view.findViewById(R.id.supplier_phone);
        Button sale = (Button) view.findViewById(R.id.sale_button);

        // Find the columns of book attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_TITLE);
        int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
        int isbnColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_COD_ISBN);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
        int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);

        // Read the book attributes from the Cursor for the current book
        String bookTitle = cursor.getString(titleColumnIndex);
        String bookAuthor = cursor.getString(authorColumnIndex);
        bookQuantity = cursor.getInt(quantityColumnIndex);
        String isbn = cursor.getString(isbnColumnIndex);
        double price = (cursor.getInt(priceColumnIndex)) / 100.00;
        String supplierName = cursor.getString(supplierNameColumnIndex);
        String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

        // Update the TextViews with the attributes for the current book
        titleTextView.setText(bookTitle);
        authorTextView.setText(bookAuthor);
        quantityTextView.setText(Integer.toString(bookQuantity));
        priceTextView.setText(Double.toString(price));
        isbnTextView.setText(isbn);
        supNameTextView.setText(supplierName);
        supPhoneTextView.setText(supplierPhone);
        sale.setOnClickListener(myButtonClickListener);
    }

    private View.OnClickListener myButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            View parentRow = (View) v.getParent();
            View parentParentRow = (View) parentRow.getParent();
            ListView listView = (ListView) parentParentRow.getParent();
            TextView mQuantity = (TextView) parentRow.findViewById(R.id.quantity);
            final int position = listView.getPositionForView(parentParentRow);
            long id = getItemId(position);
            String quantity = mQuantity.getText().toString().trim();
            int updatedQuantity = Integer.parseInt(quantity) - 1;
            Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI_BOOK, id);
            if (updatedQuantity < 0) {
                Toast.makeText(v.getContext(), v.getContext().getString(R.string.quantity_zero), Toast.LENGTH_LONG).show();
            } else {
                quantityTextView.setText(Integer.toString(updatedQuantity));
                if (updatedQuantity <= 3) {
                    Toast.makeText(v.getContext(), v.getContext().getString(R.string.quantity_few), Toast.LENGTH_LONG).show();
                }
                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_BOOK_QUANTITY, updatedQuantity);
                int rowsUpdated = v.getContext().getContentResolver().update(BookstoreContract.BookEntry.CONTENT_URI_BOOK, values, "_ID = ?", new String[]{String.valueOf(id)});
            }
        }

    };
}