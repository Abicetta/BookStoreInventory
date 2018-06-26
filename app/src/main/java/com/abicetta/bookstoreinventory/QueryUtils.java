package com.abicetta.bookstoreinventory;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class QueryUtils {
    String mIsbn;

    private QueryUtils(String isbn) {
        mIsbn = isbn;
    }

    private static final String LOG_TAG = QueryUtils.class.getName();

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the article JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            try {
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return " ";
            }
        }
        return output.toString();
    }

    /**
     * parsing a JSON response.
     */
    private static BooksOb extractFeatureFromJson(String jsonResponse) throws JSONException {
        String title;
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }
        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        JSONObject baseJsonResponse = new JSONObject(jsonResponse);
        int totalItems = baseJsonResponse.optInt("totalItems");
        if (totalItems == 0) {
            return null;
        } else {
            JSONArray items = baseJsonResponse.getJSONArray("items");
            JSONObject currentVolume = items.getJSONObject(0);  // There'll be only 1 book per ISBN
            JSONObject volumeInfo = currentVolume.getJSONObject("volumeInfo");
            String titleString = volumeInfo.optString("title");
            String subtitle = volumeInfo.optString("subtitle");
            if (subtitle == null || subtitle.equals("")) {
                title = titleString;
            } else {
                subtitle = ", " + subtitle;
                title = titleString + subtitle;
            }
            JSONArray authorsArray = volumeInfo.getJSONArray("authors");
            String authors = "";
            for (int i = 0; i < authorsArray.length(); i++) {
                if (i == 0) {
                    authors = authorsArray.getString(i);
                } else {
                    // Get a single article at position i within the list of articles
                    authors = authors + ", " + authorsArray.getString(i);
                }
            }
            final BooksOb data = new BooksOb(title, authors);
            data.setBookTitle(title);
            data.setBookAuthor(authors);
            return data;
        }
    }

    public static BooksOb fetchBooksData(String requestUrl) throws JSONException {
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        // Extract relevant fields from the JSON response and create a list of {@link Article}s
        BooksOb books = extractFeatureFromJson(jsonResponse);
        // Return the list of {@link Article}s
        return books;
    }
}