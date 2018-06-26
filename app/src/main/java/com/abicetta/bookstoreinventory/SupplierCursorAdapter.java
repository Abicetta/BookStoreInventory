package com.abicetta.bookstoreinventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.abicetta.bookstoreinventory.data.BookstoreContract;

/**
 * {@link SupplierCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of suppliers data as its data source. This adapter knows
 * how to create list items for each row of suppliers data in the {@link Cursor}.
 */
public class SupplierCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link SupplierCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public SupplierCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created supplier_item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.supplier_item, parent, false);
    }

    /**
     * This method binds the supplier data (in the current row pointed to by cursor) to the given
     * suppliers list layout. For example, the name for the current supplier can be set on the name TextView
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
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView phoneTextView = (TextView) view.findViewById(R.id.phone);
        // Find the columns of supplier attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(BookstoreContract.SupplierEntry.COLUMN_NAME_SUPPLIER);
        int phoneColumnIndex = cursor.getColumnIndex(BookstoreContract.SupplierEntry.COLUMN_PHONE_SUPPLIER);
        // Read the supplier attributes from the Cursor for the current supplier
        String supplierName = cursor.getString(nameColumnIndex);
        String supplierPhone = cursor.getString(phoneColumnIndex);
        // Update the TextViews with the attributes for the current book
        nameTextView.setText(supplierName);
        phoneTextView.setText(supplierPhone);
    }
}