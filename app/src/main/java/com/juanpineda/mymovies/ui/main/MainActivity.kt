package com.juanpineda.mymovies.ui.main

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.juanpineda.mymovies.databinding.ActivityMainBinding
import com.juanpineda.mymovies.ui.common.PermissionRequester
import com.juanpineda.mymovies.ui.common.startActivity
import com.juanpineda.mymovies.ui.detail.DetailActivity
import com.juanpineda.mymovies.ui.main.MainViewModel.UiModel
import org.koin.androidx.scope.ScopeActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ScopeActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MoviesAdapter
    private val coarsePermissionRequester =
        PermissionRequester(this, ACCESS_COARSE_LOCATION)

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MoviesAdapter(viewModel::onMovieClicked)
        binding.recycler.adapter = adapter
        viewModel.model.observe(this, Observer(::updateUi))
    }

    private fun updateUi(model: UiModel) {
        binding.progress.visibility = if (model is UiModel.Loading) VISIBLE else GONE
        when (model) {
            is UiModel.Content -> adapter.movies = model.movies
            is UiModel.Navigation -> startActivity<DetailActivity> {
                putExtra(DetailActivity.MOVIE, model.movie.id)
            }
            UiModel.Error -> showError()
            UiModel.RequestLocationPermission -> coarsePermissionRequester.request {
                viewModel.onCoarsePermissionRequested()
            }
        }
    }

    private fun showError(){
        binding.layoutError.layoutViewError.visibility = VISIBLE
        binding.layoutError.buttonError.setOnClickListener {
            binding.layoutError.layoutViewError.visibility = GONE
            viewModel.refresh()
        }
    }
}