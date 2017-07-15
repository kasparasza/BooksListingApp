package com.example.kasparasza.bookslistingapp;

import android.app.Activity;
import android.content.Context;
import android.util.JsonReader;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom class that holds static variables and methods required by other classes & activities of the app.
 */

public class AppUtilities {

    // String constants used:
    private static final String LOG_TAG = AppUtilities.class.getSimpleName();
    private static final String NO_TITLE = "no title is available";
    private static final String NO_AUTHOR = "no author is available";
    private static final String NO_DESCRIPTION = "no description is available";

    /**
     * Create a private constructor because no one should ever create a {@link AppUtilities} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name AppUtilities (and an object instance of AppUtilities is not needed).
     */
    private AppUtilities() {
    }

    ////
    /* Utility methods that are used to implement an http query and read information from it:
    ////

    /**
     * Executes calls to helper methods and returns a List of Book objects
     * @param stringWithHttpQuery string that contains URL query
     * @return List<Book> a List of Book objects
     */
    static List<Book> getDataFromHttp(String stringWithHttpQuery) {
        String JSONString = "";
        List<Book> bookList = new ArrayList<Book>();

        // check whether input String is valid
        if (stringWithHttpQuery == null) {
            return bookList;
        } else {
            // call a method that transforms String into Url
            URL url = createUrl(stringWithHttpQuery);

            // get http response as a JSON String
            try {
                JSONString = performHttpConnection(url);
                bookList = extractFromJSONString(JSONString);
            } catch (IOException exc_03) {
                Log.e(LOG_TAG, "Http connection was not successful " + exc_03);
            }
        }
        return bookList;
    }

    /**
     * Creates an URL object from an input String
     *
     * @param stringWithHttpQuery string that contains URL query
     * @return URL object
     */
    static private URL createUrl(String stringWithHttpQuery) {
        URL urlWithHttpQuery = null;
        try {
            urlWithHttpQuery = new URL(stringWithHttpQuery);
        } catch (MalformedURLException exc_01) {
            Log.e(LOG_TAG, "The app was not able to create a URL request from the query " + exc_01);
        }
        return urlWithHttpQuery;
    }

    /**
     * Uses URL object to create and execute Http connection, obtains InputStream and calls a helper method to read it
     *
     * @param url URL query
     * @return received JSON response in a String format
     */
    static private String performHttpConnection(URL url) throws IOException {
        String JSONResponse = "";
        if (url == null) {
            return JSONResponse;
        }
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000 /* milliseconds */);
            httpURLConnection.setConnectTimeout(15000 /* milliseconds */);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            // check whether the connection response code is appropriate (in this case == 200)
            if (httpURLConnection.getResponseCode() == 200) {
                inputStream = httpURLConnection.getInputStream();
                JSONResponse = readInputStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Bad response from the server was received - response code: " + httpURLConnection.getResponseCode());
            }
        } catch (IOException exc_02) {
            Log.e(LOG_TAG, "IOE exception was encountered when trying to connect to http " + exc_02);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return JSONResponse;
    }


    /**
     * Reads InputStream and parses it into a String
     *
     * @param stream InputStream
     * @return String
     */
    static private String readInputStream(InputStream stream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        if (stream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Reads JSONString and extracts relevant data from it
     *
     * @param JSONString - result of the previous http query parsed into String format
     * @return List<Book> a list of Book objects
     */
    static private List<Book> extractFromJSONString(String JSONString) {
        List<Book> bookDataList = new ArrayList<Book>();
        try {
            // convert String to a JSONObject
            JSONObject jsonObject = new JSONObject(JSONString);

            // extract "items" JSONArray
            JSONArray arrayItems = jsonObject.getJSONArray("items");

            // Loop through each item in the array
            // Get Book JSONObject at position i
            int item;
            for (item = 0; item < arrayItems.length(); item++) {
                JSONObject bookInfo = arrayItems.getJSONObject(item);

                // get "volumeInfo" JSONObject
                JSONObject volumeInfoObject = bookInfo.getJSONObject("volumeInfo");

                // extract "title" for title of a book
                String title = "";
                try {
                    title = volumeInfoObject.getString("title");
                } catch (org.json.JSONException exc_07) {
                    title = NO_TITLE;
                }

                // extract "authors" for authors of a book
                String authors = "";
                // step_1 authors are stored in JSONArray
                try {
                    JSONArray arrayAuthors = volumeInfoObject.getJSONArray("authors");
                    // step_2 read contents of the JSONArray and create a String
                    int i;
                    StringBuilder stringBuilder = new StringBuilder();
                    for (i = 0; i < arrayAuthors.length(); i++) {
                        stringBuilder.append(arrayAuthors.getString(i)).append(", "); // Use ", " as the delimiter
                    }
                    authors = stringBuilder.toString();
                    authors = authors.substring(0, authors.length() - 2); // Delete ", " from the end of the String
                } catch (org.json.JSONException exc_07) {
                    authors = NO_AUTHOR;
                }

                // extract "description" for description of a book
                String description = "";
                try {
                    description = volumeInfoObject.getString("description");
                } catch (org.json.JSONException exc_07) {
                    description = NO_DESCRIPTION;
                }

                // get "imageLinks" JSONObject & extract an image link address for a book
                JSONObject imageLinksObject;
                String imageLink;
                // check whether "imageLinks" JSONObject is available
                if (volumeInfoObject.has("imageLinks")) {
                    // get "imageLinks" JSONObject
                    imageLinksObject = volumeInfoObject.getJSONObject("imageLinks");
                    // extract "thumbnail" for image link address of a book
                    try {
                        imageLink = imageLinksObject.getString("thumbnail");
                    } catch (org.json.JSONException exc_07) {
                        // if "thumbnail" String is not available, try to extract "smallThumbnail" String
                        try {
                            imageLink = imageLinksObject.getString("smallThumbnail");
                        } catch (org.json.JSONException exc_08) {
                            // if no String with http is available, we set the String to be empty
                            // there will have to be an additional check to be performed in Adapter class
                            // when we try to load the image with Picasso
                            imageLink = "";
                        }
                    }
                } else {
                    imageLink = "";
                }

                // create Book object from the extracted data
                Book book = new Book(title, authors, description, imageLink);

                // add the object to List
                bookDataList.add(book);
            }
        } catch (JSONException exc_04) {
            Log.e(LOG_TAG, "An exception was encountered while trying to read JSONString " + exc_04);
        }
        // return result of the method
        return bookDataList;
    }

    ////
    /* Utility methods that are used to work with user search queries:
    ////

    /**
     * Transforms user's search query parameters into a String formatted for URL
     * @param query - user's search query String
     * @return queryFormattedForUrl - String formatted for URL
     */
    static String formatStringForUrl(String query) {
        String stringFormattedForUrl = null;
        try {
            stringFormattedForUrl = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException exc_05) {
            Log.e(LOG_TAG, "An exception was encountered while trying to convert the search query into URL " + exc_05);
        }
        return stringFormattedForUrl;
    }

}
