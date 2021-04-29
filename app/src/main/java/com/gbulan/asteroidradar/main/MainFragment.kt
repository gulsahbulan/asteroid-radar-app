package com.gbulan.asteroidradar.main

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gbulan.asteroidradar.R
import com.gbulan.asteroidradar.network.NasaApi
import com.gbulan.asteroidradar.database.getDatabase
import com.gbulan.asteroidradar.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment() {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this, MainViewModel.Factory(
                NasaApi.retrofitService, getDatabase(requireContext()).asteroidDao
            )
        ).get(MainViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.rvAsteroid.adapter = MainAdapter(MainAdapter.OnclickListener {
            viewModel.displayAsteroidDetails(it)
        })
        val constraintLayout: ConstraintLayout = binding.constraintLayoutMainFragment

        viewModel.snackBar.observe(viewLifecycleOwner) { text ->
            text?.let {
                val snackBar: Snackbar =
                    Snackbar.make(constraintLayout, text, Snackbar.LENGTH_INDEFINITE)
                snackBar.setAction("RETRY") {
                    viewModel.onSnackBarShown()
                }
                snackBar.setActionTextColor(
                    resources.getColor(R.color.colorAccent, requireContext().theme)
                )
                snackBar.show()
            }
        }

        viewModel.navigateToSelectedAsteroid.observe(viewLifecycleOwner, {
            if (it != null) {
                findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                viewModel.displayAsteroidDetailsComplete()
            }
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_week_menu -> viewModel.showWeekAsteroids()
            R.id.show_today_menu -> viewModel.showTodayAsteroids()
            R.id.show_saved_menu -> viewModel.showSavedAsteroids()
        }
        return true
    }
}
