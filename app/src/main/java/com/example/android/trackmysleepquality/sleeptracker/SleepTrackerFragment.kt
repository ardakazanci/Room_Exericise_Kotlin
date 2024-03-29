/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Fragment görünümü ile Fragment Class ' ını Binding olarak bağlıyoruz.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)
        // Boş olmaması için requireNotNull olması gerekmekte. İleride application ı kullanacağız.
        val application = requireNotNull(this.activity).application
        // Veri Kaynağını alıyoruz. Verilere ulaşmak içinde dao kullancağız.
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        // Fabrika Tasarım Prensibiyle ViewModel oluştuyoruz.
        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)
        // Oluşturulan ViewModel ' ı Fragment ' ımıza bağlıyoruz.
        val sleepTrackerViewModel = ViewModelProviders.of(this, viewModelFactory).get(SleepTrackerViewModel::class.java)


        /**
         * Pratik olarak parametreli bir viewModel oluştururken fabric kullanmak iyi pratiktir.
         */

        // ViewModel ' ın bağlı olduğu yaşamdöngüsü bu fragment a bağlı olacak
        binding.setLifecycleOwner(this)
        // Oluşturduğumuz viewModel ' ı binding olarak bağlıyoruz. Bu şekilde fragment görünümünde yer alan view
        // elemanları üzerinde yapılacak işlemler bu viewModel üzerinden sağlanacak, uzun vadede verilerin bağlanması, test edilmesi konusunda
        // pratik ve kolaylık sağlar. Örneğin. Fragment içerisinde yer alan 2 adet textview ' e verilerin girilip girilmediğini ViewModel da business işi yapıp
        // onCreateView içerisinde sunabiliriz. 
        binding.sleepTrackerViewModel = sleepTrackerViewModel

        sleepTrackerViewModel.navigateToSleepQuality.observe(this, Observer { night ->
            night?.let {
                // null değilse
                this.findNavController().navigate(SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(night.nightId))
                sleepTrackerViewModel.doNavigating()
            }
        })

        sleepTrackerViewModel.showSnackBarEvent.observe(this, Observer {

            if (it == true) {
                Snackbar.make(
                        activity!!.findViewById(android.R.id.content),
                        getString(R.string.cleared_message),
                        Snackbar.LENGTH_SHORT
                ).show()
                sleepTrackerViewModel.doneShowingSnackbar()
            }

        })
        return binding.root
    }
}
