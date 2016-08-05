/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.popmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.udacity.popmovies.data.MovieContract.MovieEntry;
import com.udacity.popmovies.data.MovieContract.MostPopularMoviesEntry;
import com.udacity.popmovies.data.MovieContract.HighestRatedMoviesEntry;
import com.udacity.popmovies.data.MovieContract.FavoriteEntry;


public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                MostPopularMoviesEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                HighestRatedMoviesEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                FavoriteEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from movies table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                MostPopularMoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from most_popular_movies table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                HighestRatedMoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from highest_rated_movies table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                FavoriteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from favorites table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
       This helper function deletes all records from both database tables using the database
       functions only.  This is designed to be used to reset the state of the database until the
       delete functionality is available in the ContentProvider.
     */
    public void deleteAllRecordsFromDB() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MovieEntry.TABLE_NAME, null, null);
        db.delete(MostPopularMoviesEntry.TABLE_NAME, null, null);
        db.delete(HighestRatedMoviesEntry.TABLE_NAME, null, null);
        db.delete(FavoriteEntry.TABLE_NAME, null, null);

        db.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromDB();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the WeatherProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.udacity.popmovies/movies
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_TYPE, type);

        long testMovieId = 209112;
        // content://com.udacity.popmovies/movies/209112
        type = mContext.getContentResolver().getType(MovieEntry.buildMovieUri(testMovieId));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the MovieEntry CONTENT_URI with id should return MovieEntry.CONTENT_ITEM_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);

        // content://com.udacity.popmovies/movies/most_popular
        type = mContext.getContentResolver().getType(MostPopularMoviesEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals("Error: the MostPopularMoviesEntry CONTENT_URI should return MostPopularMoviesEntry.CONTENT_TYPE",
                MostPopularMoviesEntry.CONTENT_TYPE, type);

        // content://com.udacity.popmovies/movies/highest_rated
        type = mContext.getContentResolver().getType(HighestRatedMoviesEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals("Error: the HighestRatedMoviesEntry CONTENT_URI should return HighestRatedMoviesEntry.CONTENT_TYPE",
                HighestRatedMoviesEntry.CONTENT_TYPE, type);

        // content://com.udacity.popmovies/movies/favorites
        type = mContext.getContentResolver().getType(FavoriteEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals("Error: the FavoriteEntry CONTENT_URI should return FavoriteEntry.CONTENT_TYPE",
                FavoriteEntry.CONTENT_TYPE, type);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicMoviesQuery() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();
        long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to Insert WeatherEntry into the Database", movieRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor moviesCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (moviesCursor == null) {
            fail("Get empty cursor by querying movies.");
        }
        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicWeatherQuery", moviesCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: MovieEntry Query did not properly set NotificationUri",
                    moviesCursor.getNotificationUri(), MovieEntry.CONTENT_URI);
        }

        moviesCursor.close();
    }

    public void testMovieByIdQuery() {
        ContentValues testValues = insertTestValues();
        long testMovieId = testValues.getAsLong(MovieEntry._ID);
        Uri testMovieUri = MovieEntry.buildMovieUri(testMovieId);

        Cursor movie = mContext.getContentResolver().query(
                testMovieUri,
                null,
                null,
                null,
                null
        );
        if (movie == null) {
            fail("Get empty cursor by querying movie by id.");
        }
        TestUtilities.validateCursor("Error by querying movie by id.", movie, testValues);
        assertEquals("Movie by ID query returned more than one entry. ", movie.getCount(), 1);

        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Movie by ID Query did not properly set NotificationUri",
                    movie.getNotificationUri(), testMovieUri);
        }
        movie.close();
    }

    public void testMostPopularMoviesQuery() {
        ContentValues testValues = insertTestValues();
        long movieId = testValues.getAsLong(MovieContract.MovieEntry._ID);

        insertReferenceTableMovieId(MostPopularMoviesEntry.TABLE_NAME, movieId);

        Cursor movies = mContext.getContentResolver().query(
                MostPopularMoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        if (movies == null) {
            fail("Get empty cursor by querying movies.");
        }
        TestUtilities.validateCursor("Error by querying movies.", movies, testValues);

        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Movies Query did not properly set NotificationUri",
                    MostPopularMoviesEntry.CONTENT_URI, movies.getNotificationUri());
        }
        movies.close();
    }

    public void testHighestRatedMoviesQuery() {
        ContentValues testValues = insertTestValues();
        long movieId = testValues.getAsLong(MovieEntry._ID);

        insertReferenceTableMovieId(HighestRatedMoviesEntry.TABLE_NAME, movieId);

        Cursor movies = mContext.getContentResolver().query(
                HighestRatedMoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        if (movies == null) {
            fail("Get empty cursor by querying movies.");
        }
        TestUtilities.validateCursor("Error by querying movies.", movies, testValues);

        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Movies Query did not properly set NotificationUri",
                    HighestRatedMoviesEntry.CONTENT_URI, movies.getNotificationUri());
        }
        movies.close();
    }

    public void testFavoriteMoviesQuery() {
        ContentValues testValues = insertTestValues();
        long movieId = testValues.getAsLong(MovieEntry._ID);

        insertReferenceTableMovieId(FavoriteEntry.TABLE_NAME, movieId);

        Cursor movies = mContext.getContentResolver().query(
                FavoriteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        if (movies == null) {
            fail("Get empty cursor by querying movies.");
        }
        TestUtilities.validateCursor("Error by querying movies.", movies, testValues);

        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Movies Query did not properly set NotificationUri",
                    FavoriteEntry.CONTENT_URI, movies.getNotificationUri());
        }
        movies.close();
    }

    public void testInsertBasicMovie() {
        ContentValues testValues = TestUtilities.createMovieValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver moviesObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, moviesObserver);

        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieUri != null);

        // Did our content observer get called?
        moviesObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(moviesObserver);

        long movieRowId = ContentUris.parseId(movieUri);
        // Verify we got a row back.
        assertTrue(movieRowId != -1);
        assertEquals(MovieEntry.buildMovieUri(movieRowId), movieUri);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.
        Cursor movies = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsert. Error validating MovieEntry.",
                movies, testValues);

        movies.close();
    }

    public void testInsertMostPopularMovie() {
        ContentValues testValues = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieUri != null);
        long movieRowId = ContentUris.parseId(movieUri);
        assertTrue(movieRowId != -1);

        TestUtilities.TestContentObserver observer = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MostPopularMoviesEntry.CONTENT_URI, true, observer);

        ContentValues entryValues = new ContentValues();
        entryValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieRowId);

        Uri entryUri = mContext.getContentResolver().insert(MostPopularMoviesEntry.CONTENT_URI, entryValues);
        assertTrue(entryUri != null);

        // Did our content observer get called?
        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        Cursor movies = mContext.getContentResolver().query(
                MostPopularMoviesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        assertNotNull(movies);
        TestUtilities.validateCursor("Error validating MostPopularMoviesEntry.", movies, entryValues);

        movies.close();
    }

    public void testInsertHighestRatedMovie() {
        ContentValues testValues = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieUri != null);
        long movieRowId = ContentUris.parseId(movieUri);
        assertTrue(movieRowId != -1);

        TestUtilities.TestContentObserver observer = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(HighestRatedMoviesEntry.CONTENT_URI, true, observer);

        ContentValues entryValues = new ContentValues();
        entryValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieRowId);

        Uri entryUri = mContext.getContentResolver().insert(HighestRatedMoviesEntry.CONTENT_URI, entryValues);
        assertTrue(entryUri != null);

        // Did our content observer get called?
        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        Cursor movies = mContext.getContentResolver().query(
                HighestRatedMoviesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        assertNotNull(movies);
        TestUtilities.validateCursor("Error validating HighestRatedMoviesEntry.", movies, entryValues);

        movies.close();
    }

    public void testInsertFavoriteMovie() {
        ContentValues testValues = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieUri != null);
        long movieRowId = ContentUris.parseId(movieUri);
        assertTrue(movieRowId != -1);

        TestUtilities.TestContentObserver observer = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(FavoriteEntry.CONTENT_URI, true, observer);

        ContentValues entryValues = new ContentValues();
        entryValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieRowId);

        Uri entryUri = mContext.getContentResolver().insert(FavoriteEntry.CONTENT_URI, entryValues);
        assertTrue(entryUri != null);

        // Did our content observer get called?
        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        Cursor movies = mContext.getContentResolver().query(
                FavoriteEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        assertNotNull(movies);
        TestUtilities.validateCursor("Error validating FavoriteEntry.", movies, entryValues);

        movies.close();
    }

    /*
        This test uses the provider to insert and then update the data. Uncomment this test to
        see if your update location is functioning correctly.
     */
    public void testUpdateMovie() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createMovieValues();

        Uri movieUri = mContext.getContentResolver().
                insert(MovieEntry.CONTENT_URI, values);
        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);
        Log.d(LOG_TAG, "New row id: " + movieRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MovieEntry._ID, movieRowId);
        updatedValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, 6.31);

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        movieCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                MovieEntry.CONTENT_URI, updatedValues, MovieEntry._ID + "= ?",
                new String[] { Long.toString(movieRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        movieCursor.unregisterContentObserver(tco);
        movieCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,   // projection
                MovieEntry._ID + " = " + movieRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    public void testDeleteAllMovies() {
        ContentValues testValues = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieUri != null);
        long id = ContentUris.parseId(movieUri);
        assertTrue(id != -1);
        assertEquals(MovieEntry.buildMovieUri(id), movieUri);

        TestUtilities.TestContentObserver moviesObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, moviesObserver);

        TestUtilities.TestContentObserver movieByIdObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(movieUri, true, movieByIdObserver);

        deleteAllRecordsFromProvider();

        moviesObserver.waitForNotificationOrFail();
        movieByIdObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(moviesObserver);
        mContext.getContentResolver().unregisterContentObserver(movieByIdObserver);
    }

    public void testDeleteMovieById() {
        ContentValues testValues = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieUri != null);
        long id = ContentUris.parseId(movieUri);
        assertTrue(id != -1);
        assertEquals(MovieEntry.buildMovieUri(id), movieUri);

        TestUtilities.TestContentObserver moviesObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, moviesObserver);

        TestUtilities.TestContentObserver movieByIdObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(movieUri, true, movieByIdObserver);

        mContext.getContentResolver().delete(
                MovieEntry.buildMovieUri(id),
                null,
                null
        );

        moviesObserver.waitForNotificationOrFail();
        movieByIdObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(moviesObserver);
        mContext.getContentResolver().unregisterContentObserver(movieByIdObserver);
    }

    public void testDeleteMostPopularMovies() {
        ContentValues testValues = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieUri != null);
        long movieRowId = ContentUris.parseId(movieUri);
        assertTrue(movieRowId != -1);

        ContentValues entryValues = new ContentValues();
        entryValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieRowId);

        Uri entryUri = mContext.getContentResolver().insert(MostPopularMoviesEntry.CONTENT_URI, entryValues);
        assertTrue(entryUri != null);

        TestUtilities.TestContentObserver observer = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MostPopularMoviesEntry.CONTENT_URI, true, observer);

        mContext.getContentResolver().delete(
                MostPopularMoviesEntry.CONTENT_URI,
                null,
                null
        );

        // Did our content observer get called?
        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        Cursor movies = mContext.getContentResolver().query(
                MostPopularMoviesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        assertNotNull(movies);
        assertTrue(movies.getCount() == 0);

        movies.close();
    }

    public void testDeleteHighestRatedMovies() {
        ContentValues testValues = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieUri != null);
        long movieRowId = ContentUris.parseId(movieUri);
        assertTrue(movieRowId != -1);

        ContentValues entryValues = new ContentValues();
        entryValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieRowId);

        Uri entryUri = mContext.getContentResolver().insert(HighestRatedMoviesEntry.CONTENT_URI, entryValues);
        assertTrue(entryUri != null);

        TestUtilities.TestContentObserver observer = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(HighestRatedMoviesEntry.CONTENT_URI, true, observer);

        mContext.getContentResolver().delete(
                HighestRatedMoviesEntry.CONTENT_URI,
                null,
                null
        );

        // Did our content observer get called?
        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        Cursor movies = mContext.getContentResolver().query(
                HighestRatedMoviesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        assertNotNull(movies);
        assertTrue(movies.getCount() == 0);

        movies.close();
    }

    public void testDeleteFavorites() {
        ContentValues testValues = TestUtilities.createMovieValues();
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieUri != null);
        long movieRowId = ContentUris.parseId(movieUri);
        assertTrue(movieRowId != -1);

        ContentValues entryValues = new ContentValues();
        entryValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieRowId);

        Uri entryUri = mContext.getContentResolver().insert(FavoriteEntry.CONTENT_URI, entryValues);
        assertTrue(entryUri != null);

        TestUtilities.TestContentObserver observer = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(FavoriteEntry.CONTENT_URI, true, observer);

        mContext.getContentResolver().delete(
                FavoriteEntry.CONTENT_URI,
                null,
                null
        );

        // Did our content observer get called?
        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        Cursor movies = mContext.getContentResolver().query(
                FavoriteEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        assertNotNull(movies);
        assertTrue(movies.getCount() == 0);

        movies.close();
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertMovieValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieEntry._ID, i);
            movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, "Test movie title " + i);
            movieValues.put(MovieEntry.COLUMN_POSTER_PATH, "http://testposterpath.com/" + i);
            movieValues.put(MovieEntry.COLUMN_OVERVIEW, "Test movie overview " + i);
            movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, System.currentTimeMillis());
            movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, 3.1 + i);
            returnContentValues[i] = movieValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        deleteAllRecords();
        // Now we can bulkInsert some movies.  In fact, we only implement BulkInsert for MovieEntry.
        // With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
            bulkInsertContentValues[i].get(MovieEntry.COLUMN_VOTE_AVERAGE);
        }
        cursor.close();
    }

    private ContentValues insertTestValues() {
        MovieDbHelper dbHelper = new MovieDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createMovieValues();
        long id = db.insert(MovieEntry.TABLE_NAME, null, testValues);
        if (id == -1) {
            fail("Error by inserting contentValues into database.");
        }
        db.close();
        return testValues;
    }

    private void insertReferenceTableMovieId(String tableName, long movieId) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieId);

        long id = db.insert(tableName, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", id != -1);
        db.close();
    }
}
