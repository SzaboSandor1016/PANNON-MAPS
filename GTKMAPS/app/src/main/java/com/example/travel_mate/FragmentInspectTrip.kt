package com.example.travel_mate

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.travel_mate.databinding.FragmentInspectTripBinding
import kotlinx.coroutines.launch
import kotlin.getValue

/*// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"*/

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentInspectTrip.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentInspectTrip : Fragment() {
    /*// TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null*/

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentInspectTrip.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(/*param1: String, param2: String*/) =
            FragmentInspectTrip().apply {
                /*arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }*/
            }
    }

    private var _binding: FragmentInspectTripBinding? = null
    val binding get() = _binding!!

    private val viewModelMain: ViewModelMain by activityViewModels { Application.factory }
    private val viewModelUser: ViewModelUser by activityViewModels { Application.factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInspectTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** [ViewModelMain.mainInspectTripState] observer
         *  observe the [viewModelMain]'s [ViewModelMain.mainInspectTripState]
         *  on state update
         *  - call [handleInspectedTripChange]
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModelMain.mainInspectTripState.collect {

                    handleInspectedTripChange(
                        editing = it.editing,
                        start = it.start,
                        inspectedTripIdentifier = it.inspectedTripIdentifier
                    )
                }
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    //Methods for the buttons responsible for opening the fragment for saving or sharing a trip
//_________________________________________________________________________________________________________________________
// BEGINNING OF TRIP METHODS
//_________________________________________________________________________________________________________________________

    private fun handleInspectedTripChange(
        editing: Boolean,
        start: String?,
        inspectedTripIdentifier: TripRepository.TripIdentifier?
    ) {

        Log.d("editing", editing.toString())

        if (inspectedTripIdentifier == null || editing) {

            removeInspectedTripListeners()

        } else {

            showHideEditInspectedTripButton(
                permissionToUpdate = inspectedTripIdentifier.permissionToUpdate
            )

            setupInspectedTripListeners()


            showInspectedTripData(
                startPlace = start!!,
                inspectedTripIdentifier = inspectedTripIdentifier
            )
        }
    }

    private fun showHideEditInspectedTripButton(permissionToUpdate: Boolean) {

        if (permissionToUpdate) {

            binding.editTripPlaces.visibility = View.VISIBLE
        } else {

            binding.editTripPlaces.visibility = View.GONE
        }
    }

    private fun showInspectedTripData(startPlace: String, inspectedTripIdentifier: TripRepository.TripIdentifier) {

        binding.tripCreatorUsername.setText(inspectedTripIdentifier.creatorUsername.toString())

        binding.tripTitle.setText(inspectedTripIdentifier.title.toString())

        binding.tripStart.setText(startPlace)
    }

    private fun setupInspectedTripListeners() {

        binding.editTripPlaces.setOnClickListener { l ->

            viewModelMain.editInspectedTrip()
        }

        binding.dismissInspectTrip.setOnClickListener { l ->

            viewModelMain.resetCurrentTripInRepository()

            viewModelMain.resetFullDetails()

            viewModelMain.returnToPrevContent()
        }
    }
    private fun removeInspectedTripListeners() {

        binding.editTripPlaces.setOnClickListener(null)

        binding.dismissInspectTrip.setOnClickListener(null)
    }

//_________________________________________________________________________________________________________________________
// END OF TRIP METHODS
//_________________________________________________________________________________________________________________________

}