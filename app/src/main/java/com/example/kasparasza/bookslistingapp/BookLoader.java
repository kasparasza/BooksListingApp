package com.example.kasparasza.bookslistingapp;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * BookLoader class that extends AsyncTaskLoader
 */

public class BookLoader extends AsyncTaskLoader<List<Book>> {
    private static final String LOG_TAG = AppUtilities.class.getSimpleName();
    // members of the class:
    // String array with url resources from which query data will be fetched
    private String urlString;
    // List with the data, held in cache memory of the loader
    private List<Book> listInCacheMemory;


    // constructor
    public BookLoader(Context context, String mUrlString) {
        super(context);
        urlString = mUrlString;
    }

    // before any actual load activity is started, we check whether the data
    // is already here (e.g. as in the case of orientation change)
    @Override
    protected void onStartLoading() {
        // if we have no data in cache -> we kick off loading it
        if (listInCacheMemory == null) {
            forceLoad();
        } else {
            deliverResult(listInCacheMemory);
        }
    }

    // implementation of otherwise abstract loadInBackground() method
    // the method calls an AsyncTask / http query to be performed
    @Override
    public List<Book> loadInBackground() {
        // Check if input Url string is not null or empty.
        // If that is the case, AsyncTask returns null
        if (urlString == null) {
            return null;
        }
        List<Book> bookList = AppUtilities.getDataFromHttp(urlString);
        return bookList;
    }

    // results of load method are saved for later use
    @Override
    public void deliverResult(List<Book> data) {
        listInCacheMemory = data;
        // We can do any pre-processing we want here
        // Just remember this is on the UI thread so nothing lengthy!
        super.deliverResult(data);
    }
}
