package com.example.kasparasza.bookslistingapp;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a custom List adapter which will populate a layout with data on Book objects
 */

// constructor of the adapter
public class BookListAdapter extends ArrayAdapter<Book> {
    public BookListAdapter(Context context, int resource, List<Book> books) {
        super(context, 0, books);
    }

    // overriding getView method that will create ListView items
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // instruct the method to reuse the views
        View listViewItem = convertView;
        if (listViewItem == null) {
            listViewItem = LayoutInflater.from(getContext()).inflate(R.layout.book_item_in_list_view, parent, false);
        }

        // initialise the views that will be populated with data
        TextView bookTitle = (TextView) listViewItem.findViewById(R.id.book_title);
        TextView bookAuthors = (TextView) listViewItem.findViewById(R.id.book_authors);
        TextView bookDescription = (TextView) listViewItem.findViewById(R.id.book_description);
        ImageView bookImageThumbnail = (ImageView) listViewItem.findViewById(R.id.book_image_thumbnail);

        // get each item from the List
        Book currentListItem = getItem(position);

        // populate the views with data
        bookTitle.setText(currentListItem.getTitle());
        bookAuthors.setText(currentListItem.getAuthors());
        bookDescription.setText(currentListItem.getDescription());
        // use of Picasso library to set ImageView
        // at first we check, whether the String with image link is not empty
        if (!currentListItem.getImageSmallThumbLink().matches("")) {
            Picasso.with(getContext())
                    .load(currentListItem.getImageSmallThumbLink())
                    .resize((int) getContext().getResources().getDimension(R.dimen.width_of_book_image_thumbnail), (int) getContext().getResources().getDimension(R.dimen.height_of_book_image_thumbnail))
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.no_image_to_download)
                    .centerInside()
                    .into(bookImageThumbnail);
        } else {
            Picasso.with(getContext())
                    .load(R.drawable.no_image_to_download)
                    .resize((int) getContext().getResources().getDimension(R.dimen.width_of_book_image_thumbnail), (int) getContext().getResources().getDimension(R.dimen.height_of_book_image_thumbnail))
                    .centerInside()
                    .into(bookImageThumbnail);
        }

        // returns an inflated ListView Item
        return listViewItem;
    }
}
