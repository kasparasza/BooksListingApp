package com.example.kasparasza.bookslistingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

public class BookDetailsActivity extends AppCompatActivity {

    // declaration of String constants to be used
    private static final String LOG_TAG = AppUtilities.class.getSimpleName();

    // initialise the views that will be populated with data
    private TextView bookTitle;
    private TextView bookAuthors;
    private TextView bookDescription;
    private ImageView bookImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // initialise the views that will be populated with data
        bookTitle = (TextView) findViewById(R.id.book_title);
        bookAuthors = (TextView) findViewById(R.id.book_authors);
        bookDescription = (TextView) findViewById(R.id.book_description);
        bookImage = (ImageView) findViewById(R.id.book_image_thumbnail);

        // populate the views with data
        // data is extracted from an Intent
        Book book = (getIntent().getParcelableExtra(Book.BOOK));
        bookTitle.setText(book.getTitle());
        bookAuthors.setText(book.getAuthors());
        bookDescription.setText(book.getDescription());
        String imageLink = book.getImageSmallThumbLink();

        // use of Picasso library to set ImageView
        // at first we check, whether the String with image link is not empty
        if (!imageLink.matches("")){
            Picasso.with(getApplicationContext())
                    .load(imageLink)
                    .resize((int) getApplicationContext().getResources().getDimension(R.dimen.width_of_book_image), (int) getApplicationContext().getResources().getDimension(R.dimen.height_of_book_image))
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.no_image_to_download)
                    .centerInside()
                    .into(bookImage);
        } else {
            Picasso.with(getApplicationContext())
                    .load(R.drawable.no_image_to_download)
                    .resize((int) getApplicationContext().getResources().getDimension(R.dimen.width_of_book_image), (int) getApplicationContext().getResources().getDimension(R.dimen.height_of_book_image))
                    .centerInside()
                    .into(bookImage);
        }
    }
}
