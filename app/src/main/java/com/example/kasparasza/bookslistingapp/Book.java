package com.example.kasparasza.bookslistingapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Book class - a class that holds Book objects.
 */

public class Book implements Parcelable{
    // elements of each class object
    private String title;
    private String authors;
    private String description;
    private String imageSmallThumbLink;

    // declaration of String constants used by the class
    protected static final String BOOK = "BOOK";

    // constructors of the class
    public Book(String mTitle, String mAuthors, String mDescription, String mImageSmallThumbLink){
        title = mTitle;
        authors = mAuthors;
        description = mDescription;
        imageSmallThumbLink = mImageSmallThumbLink;
    }

    // methods of the class
    public String getTitle(){
        return title;
    }

    public String getAuthors(){
        return authors;
    }

    public String getDescription(){
        return description;
    }

    public String getImageSmallThumbLink(){
        return imageSmallThumbLink;
    }


    //// The following methods are required for using Parcelable:

    private Book(Parcel in) {
        // The order must match the order in writeToParcel()
        title = in.readString();
        authors = in.readString();
        description = in.readString();
        imageSmallThumbLink = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(authors);
        out.writeString(description);
        out.writeString(imageSmallThumbLink);
    }

    // method required to be implemented by Parcelable
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

}
