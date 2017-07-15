package com.example.kasparasza.bookslistingapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BookSearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>>, AdapterView.OnItemSelectedListener {

    // declaration of String constants to be used
    private static final String LOG_TAG = AppUtilities.class.getSimpleName();
    private static final String LIST_VIEW_ITEM_INDEX = "LIST_VIEW_ITEM_INDEX";
    private static final String LIST_VIEW_TOP = "LIST_VIEW_TOP";
    private static final String BOOK_PARCELABLE_LIST = "BOOK_PARCELABLE_LIST";
    private static final String SEARCH_VIEW_VISIBILITY = "SEARCH_VIEW_VISIBILITY";
    private static final String BOOK_TITLE = "BOOK_TITLE";
    private static final String BOOK_AUTHOR = "BOOK_AUTHOR";
    private static final String BOOK_DESCRIPTION = "BOOK_DESCRIPTION";
    private static final String BOOK_IMAGE_LINK = "BOOK_IMAGE_LINK";
    // base parts of the Google Books API query
    private static final String URL_BASE = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String QUERY_PARAM_IN_TITLE = "intitle:";
    private static final String QUERY_PARAM_IN_AUTHOR = "inauthor:";
    private static final String QUERY_PARAM_MAX_RESULTS = "maxResults=";
    private static final String QUERY_PRINT_TYPE_BOOKS = "&printType=books";
    // the initial static url query
    final String STRING_BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=subject:fiction+printType=books&maxResults=10&orderBy=newest";
    // String that stores users selection for the maximum number of search results
    String maxSearchResults;
    // declaration of layout views
    private ListView bookSearchResultsListView;
    private LinearLayout searchQueryView;
    private LinearLayout searchQueryViewHideable;
    private EditText titleSearchInput;
    private EditText authorSearchInput;
    private Button searchButton;
    private Spinner spinner;
    private TextView noResultsView;
    private ProgressBar progressBar;
    private ImageView noNetworkConnection;
    private TextView expandViewText;
    // declaration of class members:
    // ArrayList that will store search results
    private ArrayList<Book> booksArrayList = new ArrayList<Book>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        // initialisation of layout views
        bookSearchResultsListView = (ListView) findViewById(R.id.search_results);
        searchQueryView = (LinearLayout) findViewById(R.id.search_query_view);
        searchQueryViewHideable = (LinearLayout) findViewById(R.id.search_query_part_that_can_be_hidden);
        titleSearchInput = (EditText) findViewById(R.id.search_query_title);
        authorSearchInput = (EditText) findViewById(R.id.search_query_author);
        searchButton = (Button) findViewById(R.id.search_button);
        spinner = (Spinner) findViewById(R.id.search_results_number);
        noResultsView = (TextView) findViewById(R.id.empty_state_text);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        noNetworkConnection = (ImageView) findViewById(R.id.no_connection_image);
        expandViewText = (TextView) findViewById(R.id.expand_search_view);

        // set ProgressBar to be not visible, as: i) if the activity is created for the first time - there is no
        // active search; ii) if the activity is recreated after device rotation (during the active search and
        // before onLoadFinished is called) - the current functionality still requires user to re-initiate the search
        // therefore, ProgressBar has to be inactive
        progressBar.setVisibility(View.GONE);

        // add onClickListener for the search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateLoader();
                // hide the soft input method keyboard after button click
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchButton.getWindowToken(), 0);
            }
        });

        // implementation of initial content to be displayed before the user enters any search query
        // if there is a network connection, a Loader is initialised with a query for initial content
        if (checkNetworkConnection()) {
            getSupportLoaderManager().initLoader(1, null, this);
            // while the query is ongoing - a progress bar is shown
            progressBar.setVisibility(View.VISIBLE);
        }

        // if there is no network connection at the time of initial onCreate, call a method that informs the user
        // Note: this check is for initial onCreate only; onRestore state is implemented separately
        if (!checkNetworkConnection() && savedInstanceState == null) {
            informAboutNoNetworkConnection();
        }

        // implementation of the Spinner:
        // Create an ArrayAdapter using the: string array with available Spinner selections and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.number_of_search_results_string_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        // Apply the selection listener to the spinner
        spinner.setOnItemSelectedListener(this);

        // Implementation of OnTouchListener for the Search View part of the layout.
        // Touch event restores visibility of the full Search View
        searchQueryView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchQueryViewHideable.setVisibility(View.VISIBLE);
                expandViewText.setText("");
                return false;
            }
        });


        //sets ClickListener for each ListView Item:
        //click action starts a new activity, where a selected Book is displayed in detail
        bookSearchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get selected book
                Book selectedBook = booksArrayList.get(position);
                // create an Intent
                Intent openBookDetailsActivity = new Intent(getApplicationContext(), BookDetailsActivity.class);
                // put extra information into the Intent
                openBookDetailsActivity.putExtra(BOOK_TITLE, selectedBook.getTitle());
                openBookDetailsActivity.putExtra(BOOK_AUTHOR, selectedBook.getAuthors());
                openBookDetailsActivity.putExtra(BOOK_DESCRIPTION, selectedBook.getDescription());
                openBookDetailsActivity.putExtra(BOOK_IMAGE_LINK, selectedBook.getImageSmallThumbLink());

                // start Activity
                startActivity(openBookDetailsActivity);
            }
        });
    }

    /**
     * Method that checks whether there is a network connection
     *
     * @return boolean that is true is there is a connection
     */
    public boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
    }

    /**
     * Method that informs User about no network connectivity
     */
    public void informAboutNoNetworkConnection() {
        searchQueryViewHideable.setVisibility(View.GONE);
        expandViewText.setText(R.string.search_expand_view);
        // if there is no ArrayList of Books in the memory - show an ImageView with no connectivity message
        if (booksArrayList.size() < 1) {
            bookSearchResultsListView.setVisibility(View.GONE);
            noNetworkConnection.setImageResource(R.drawable.no_network_image);
            Toast.makeText(this, R.string.no_internet_connection_message, Toast.LENGTH_SHORT).show();
        } else {
            // if there is an ArrayList of Books in the memory - keep displaying it
            // just inform user via Toast message
            Toast.makeText(this, R.string.no_internet_connection_message, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Method that: 1) reads search query entered by the user; 2) initiates a Loader and
     * 3) hides part of the views
     */
    public void initiateLoader() {
        String searchQuery = prepareSearchQuery();
        // check whether the user has entered any search query at all and
        // whether there is a network connection
        // if both a true -> actions are performed
        if (searchQuery != null && checkNetworkConnection()) {
            // Loader initialisation - starts a new or restarts an existing
            // restartLoader() is called instead of initLoader(), as this will allow to reset the Loader
            // and perform a new search query after search button is clicked
            getSupportLoaderManager().restartLoader(0, null, this);
            // hide visibility of the part of the Search View
            // this is done to increase screen space for search results
            searchQueryViewHideable.setVisibility(View.GONE);
            expandViewText.setText(R.string.search_expand_view);
            // As the Loader is called, progress bar is set here as well
            // while the query is ongoing - a progress bar is shown
            progressBar.setVisibility(View.VISIBLE);
        } else if (!checkNetworkConnection()) {
            Toast.makeText(this, R.string.no_internet_connection_message, Toast.LENGTH_SHORT).show();
            searchQueryViewHideable.setVisibility(View.GONE);
            expandViewText.setText(R.string.search_expand_view);
        }
    }

    // implementation of methods that are required by otherwise abstract LoaderManager interface:
    // #1) onCreateLoader, #2) onLoadFinished, #3) onLoaderReset
    // #1) onCreateLoader - create an instance of a Loader if there is no previous one
    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 0:
                return new BookLoader(this, prepareSearchQuery()); // Loader to load users search query
            case 1:
                return new BookLoader(this, STRING_BASE_URL); // Loader to load initial content
            default:
                return new BookLoader(this, STRING_BASE_URL);
        }
    }

    // #2) onLoadFinished - populate UI with the data obtained from http query
    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> data) {

        // create an instance of an adapter which will populate the layout with data on Book objects
        BookListAdapter adapterForSearchResults = new BookListAdapter(this, 0, data);

        // connect the adapter with the root List layout & with the ArrayList data
        bookSearchResultsListView.setAdapter(adapterForSearchResults);
        booksArrayList = (ArrayList<Book>) data;

        // when the query is finalized - the progress bar is hidden
        progressBar.setVisibility(View.GONE);

        // if the query results in zero Books to display, an appropriate message is displayed
        bookSearchResultsListView.setEmptyView(noResultsView);
        noResultsView.setText(R.string.no_results_message);
    }

    // #3) onLoaderReset - clear data on reset
    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        loader.reset();
    }

    /**
     * Method that records the state of the ListView if there is a configuration change
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // get index and top positions of the ListView
        // index - returns the top visible list item
        int index = bookSearchResultsListView.getFirstVisiblePosition();
        View view = bookSearchResultsListView.getChildAt(0);
        // returns relative offset from the top of the list
        int top = (view == null) ? 0 : (view.getTop() - bookSearchResultsListView.getPaddingTop());

        // get visibility state of Search View
        boolean searchViewVisibility = searchQueryViewHideable.isShown();

        // save items to a bundle
        outState.putInt(LIST_VIEW_ITEM_INDEX, index);
        outState.putInt(LIST_VIEW_TOP, top);
        outState.putParcelableArrayList(BOOK_PARCELABLE_LIST, booksArrayList);
        outState.putBoolean(SEARCH_VIEW_VISIBILITY, searchViewVisibility);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    /**
     * Method that restores the state of the ListView after a configuration change
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Superclass that is being always called
        super.onRestoreInstanceState(savedInstanceState);

        // get information form the Bundle
        if (savedInstanceState != null) {
            // index and top positions of the ListView
            int index = savedInstanceState.getInt(LIST_VIEW_ITEM_INDEX, 0);
            int top = savedInstanceState.getInt(LIST_VIEW_TOP, 0);

            // the ListView itself is being recreated
            booksArrayList = savedInstanceState.getParcelableArrayList(BOOK_PARCELABLE_LIST);
            BookListAdapter adapter = new BookListAdapter(this, 0, booksArrayList);
            bookSearchResultsListView.setAdapter(adapter);

            // set / restore the position of the ListView
            bookSearchResultsListView.setSelectionFromTop(index, top);

            // set / restore visibility state of the Search View
            if (!savedInstanceState.getBoolean(SEARCH_VIEW_VISIBILITY)) {
                searchQueryViewHideable.setVisibility(View.GONE);
                expandViewText.setText(R.string.search_expand_view);
            }

            // set visibility of the ProgressBar
            if (booksArrayList != null) {
                progressBar.setVisibility(View.GONE);
            }
        }

        // if there is no network connection at the time of onRestore, call a method that informs the user
        if (!checkNetworkConnection()) {
            informAboutNoNetworkConnection();
        }
    }


    /**
     * Method that creates String with http search query
     */
    public String prepareSearchQuery() {
        // search query strings entered by the user
        String titleSearchStringUnformatted = titleSearchInput.getText().toString();
        String authorSearchStringUnformatted = authorSearchInput.getText().toString();
        String fullQuery = "";
        // we call helper methods to format the query strings
        String titleQueryFormatted = AppUtilities.formatStringForUrl(titleSearchStringUnformatted);
        String authorQueryFormatted = AppUtilities.formatStringForUrl(authorSearchStringUnformatted);

        // check whether any query parameters were entered & construct the full String with http search query
        //  users preference for the max amount of search results to be displayed is a global variable
        if (titleSearchStringUnformatted.matches("") && authorSearchStringUnformatted.matches("") && !checkNetworkConnection()) {
            fullQuery = null;
        } else if (titleSearchStringUnformatted.matches("") && authorSearchStringUnformatted.matches("")) {
            // toast message is displayed only if there is network connection, if there is no connection - there is no logic to enter the search parameters
            Toast.makeText(getApplicationContext(), R.string.toast_no_search_parameters_entered, Toast.LENGTH_SHORT).show();
            fullQuery = null;
        } else if (titleSearchStringUnformatted.matches("")) {
            fullQuery = URL_BASE + QUERY_PARAM_IN_AUTHOR + authorQueryFormatted + "&" + QUERY_PARAM_MAX_RESULTS + maxSearchResults + QUERY_PRINT_TYPE_BOOKS;
        } else if (authorSearchStringUnformatted.matches("")) {
            fullQuery = URL_BASE + QUERY_PARAM_IN_TITLE + titleQueryFormatted + "&" + QUERY_PARAM_MAX_RESULTS + maxSearchResults + QUERY_PRINT_TYPE_BOOKS;
        } else {
            fullQuery = URL_BASE + QUERY_PARAM_IN_TITLE + titleQueryFormatted + "+" + QUERY_PARAM_IN_AUTHOR + authorQueryFormatted + "&" + QUERY_PARAM_MAX_RESULTS + maxSearchResults + QUERY_PRINT_TYPE_BOOKS;
        }
        return fullQuery;
    }


    /**
     * Method that listens to users selections made in the spinner
     * required for the implementation of OnItemSelectedListener
     */
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected and we retrieve the selected item
        maxSearchResults = (String) parent.getItemAtPosition(pos);
    }

    /**
     * required Method for the implementation of OnItemSelectedListener
     */
    public void onNothingSelected(AdapterView<?> parent) {
        // Method left blank
    }
}