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

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/*
    Note that this class utilizes constants that are declared with package protection inside of
    the UriMatcher, which is why the test must be in the same data package as the Android app code.
    Doing the test this way is a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {

    private static final long TEST_MOVIE_ID = 209112;

    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIE_BY_ID_DIR = MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID);
    private static final Uri TEST_MOST_POPULAR_MOVIES_DIR = MovieContract.MostPopularMoviesEntry.CONTENT_URI;
    private static final Uri TEST_HIGHEST_RATED_MOVIES_DIR = MovieContract.HighestRatedMoviesEntry.CONTENT_URI;
    private static final Uri TEST_FAVORITES_DIR = MovieContract.FavoriteEntry.CONTENT_URI;

    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The MOVIES URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIES);
        assertEquals("Error: The MOVIE BY ID URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_BY_ID_DIR), MovieProvider.MOVIE_ID);
        assertEquals("Error: The MOST POPULAR MOVIES URI was matched incorrectly.",
                testMatcher.match(TEST_MOST_POPULAR_MOVIES_DIR), MovieProvider.MOVIE_MOST_POPULAR);
        assertEquals("Error: The HIGHEST RATED MOVIES URI was matched incorrectly.",
                testMatcher.match(TEST_HIGHEST_RATED_MOVIES_DIR), MovieProvider.MOVIE_HIGHEST_RATED);
        assertEquals("Error: The BASE FAVORITES URI was matched incorrectly.",
                testMatcher.match(TEST_FAVORITES_DIR), MovieProvider.MOVIE_FAVORITE);
    }
}