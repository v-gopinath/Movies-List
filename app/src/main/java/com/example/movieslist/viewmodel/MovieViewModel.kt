package com.example.movieslist.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movieslist.utils.AppConstants
import com.example.movieslist.model.Movie
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    val moviesLiveData = MutableLiveData<ArrayList<Movie>>()
    var pageTitle = ""

    fun getMoviesListLiveData(): LiveData<ArrayList<Movie>> {
        return moviesLiveData
    }

    fun readMoviesDataFromJson(currentPageNumber: Int, context: Context) {
        viewModelScope.launch {
            var jsonFromFile: String? = null
            try {
                val inputStream: InputStream =
                    context.assets.open(AppConstants.FILE_NAME_PREFIX + currentPageNumber + AppConstants.FILE_NAME_SUFFIX)
                jsonFromFile = inputStream.bufferedReader().use { it.readText() }
                readMovies(jsonFromFile)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(
                    context,
                    AppConstants.ERROR_FILE, Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun readMovies(json: String) {
        val pageObj = JSONObject(json).getJSONObject(AppConstants.PAGE_OBJ)
        pageTitle = pageObj.getString(AppConstants.MOVIE_TITLE)
        val contentArray = pageObj.getJSONObject(AppConstants.CONTENT_ARRAY)
        var movieList: ArrayList<Movie> = ArrayList()
        if (contentArray == null || contentArray.length() == 0) {
            moviesLiveData.value?.clear()
            moviesLiveData.value = movieList
        } else {
            val contentObj = contentArray.getJSONArray(AppConstants.CONTENT_OBJ)
            for (i in 0 until contentObj.length()) {
                val contentItems = contentObj.getJSONObject(i)
                val name = contentItems.getString(AppConstants.MOVIE_NAME)
                val image = contentItems.getString(AppConstants.MOVIE_POSTER_IMG)
                movieList.add(
                    Movie(
                        name,
                        image
                    )
                )
            }
            moviesLiveData.value?.clear()
            moviesLiveData.value = movieList
        }
    }
}