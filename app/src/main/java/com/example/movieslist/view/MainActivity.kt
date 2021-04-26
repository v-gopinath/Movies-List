package com.example.movieslist.view

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movieslist.utils.AppConstants
import com.example.movieslist.R
import com.example.movieslist.adapter.MovieAdapter
import com.example.movieslist.model.Movie
import com.example.movieslist.viewmodel.MovieViewModel
import kotlinx.android.synthetic.main.activity_main.*
import ru.alexbykov.nopaginate.paginate.NoPaginate

class MainActivity : AppCompatActivity() {

    private var movieList: ArrayList<Movie> = ArrayList()

    private lateinit var adapter: MovieAdapter
    private var pageNumber: Int = 1
    private val totalPageNumber = 3
    private var noPaginate: NoPaginate? = null
    private lateinit var movieViewModel: MovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewModel()
        observeLiveData()
        initView()
    }

    private fun observeLiveData() {
        movieViewModel.getMoviesListLiveData().observe(this, Observer {
            movieList.addAll(it)
            adapter.notifyDataSetChanged()
            if (it.size > 0) {
                if (totalPageNumber > pageNumber) {
                    setUpPagination()
                } else {
                    noPaginate?.setNoMoreItems(true)
                }
            }
        })
    }

    private fun initViewModel() {
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)
    }

    private fun initView() {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setAdapter(AppConstants.SPAN_COUNT_LANDSCAPE)
        } else {
            setAdapter(AppConstants.SPAN_COUNT_POTRAIT)
        }
    }

    private fun setAdapter(count: Int) {
        rvMovies.layoutManager = GridLayoutManager(this, count)
        adapter = MovieAdapter(movieList)
        rvMovies.adapter = adapter
        movieViewModel.readMoviesDataFromJson(pageNumber, this)
        supportActionBar?.title = movieViewModel.pageTitle
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val item = menu?.findItem(R.id.action_search)
        val searchView = item?.actionView as SearchView
        searchView.queryHint = AppConstants.SEARCH_QUERY_HINT
        searchView.maxWidth = Int.MAX_VALUE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    if (query.isEmpty() || query.length >= 3) {
                        adapter.filter.filter(query)
                        noPaginate?.setNoMoreItems(true)

                        if (adapter.itemCount > 0) {
                            tvNoDataPlaceHolder.visibility = View.GONE
                        } else {
                            tvNoDataPlaceHolder.visibility = View.VISIBLE
                        }

                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (newText.isEmpty() || newText.length >= 3) {
                        adapter.filter.filter(newText)
                        Handler().postDelayed(Runnable {
                            if (adapter.itemCount > 0) {
                                tvNoDataPlaceHolder.visibility = View.GONE
                            } else {
                                tvNoDataPlaceHolder.visibility = View.VISIBLE
                            }
                        }, 200)

                        noPaginate?.setNoMoreItems(true)
                    }
                }
                return false
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_search) {
            true
        } else super.onOptionsItemSelected(item)

    }

    private fun setUpPagination() {
        if (noPaginate != null) {
            noPaginate?.setNoMoreItems(true)
        }
        noPaginate = NoPaginate.with(rvMovies)
            .setOnLoadMoreListener {
                if (totalPageNumber > pageNumber) {
                    pageNumber += 1
                    movieViewModel.readMoviesDataFromJson(pageNumber, this)
                }
            }
            .setLoadingTriggerThreshold(1)
            .build()
    }

}