package com.abicetta.bookstoreinventory;

public class BooksOb {
    private String bookTitle;
    private String bookAuthor;

    public BooksOb(String webTitle, String webAuthor) {
        bookTitle = webTitle;
        bookAuthor = webAuthor;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookTitle(String title) {
        this.bookTitle = title;
    }

    public void setBookAuthor(String author) {
        this.bookAuthor = author;
    }
}