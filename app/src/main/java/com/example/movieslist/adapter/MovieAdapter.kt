package com.example.movieslist.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.movieslist.utils.AppConstants
import com.example.movieslist.R
import com.example.movieslist.model.Movie
import kotlinx.android.synthetic.main.row_movie.view.*
import java.io.IOException
import java.io.InputStream


class MovieAdapter(movieList: ArrayList<Movie>) :
    RecyclerView.Adapter<MovieAdapter.MovieViewHolder>(), Filterable {

    private var movieList: ArrayList<Movie> = ArrayList()
    private var filterMovieList: ArrayList<Movie> = ArrayList()

    init {
        this.movieList = movieList
        this.filterMovieList = movieList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filterMovieList.size
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        var eachItem = filterMovieList[position]
        holder.itemView.tvMovieName.text = eachItem.movieName
        loadImages(eachItem.moviePoster, holder.itemView.context, holder.itemView.ivMoviePoster)
    }

    inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private fun loadImages(
        imgName: String?,
        context: Context,
        ivMoviePoster: ImageView
    ) {
        var inputStream: InputStream? = null
        try {
            inputStream = imgName?.let {
                context.assets.open(it)
            }
        } catch (ex: IOException) {
            inputStream =
                context.assets.open(AppConstants.NO_IMG_PLACEHOLDER)
        }
        val drawable = Drawable.createFromStream(inputStream, null)
        ivMoviePoster.setImageDrawable(drawable)
        inputStream?.close()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val searchQuery = constraint.toString()
                if (searchQuery.isEmpty()) {
                    filterMovieList = movieList
                } else {
                    val resultList = ArrayList<Movie>()
                    for (row in movieList) {
                        row.movieName?.let {
                            if (it.toLowerCase().contains(searchQuery.toLowerCase())) {
                                resultList.add(row)
                            }
                        }
                    }
                    filterMovieList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterMovieList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
                filterMovieList = results?.values as ArrayList<Movie>
                notifyDataSetChanged()
            }

        }
    }
}