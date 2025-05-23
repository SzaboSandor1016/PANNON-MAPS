package com.example.travel_mate

import android.annotation.SuppressLint
import android.content.res.Resources
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_mate.databinding.FragmentRouteBinding
import kotlinx.coroutines.launch
import kotlin.getValue

/*// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"*/

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentRoute.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentRoute : Fragment() {
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
         * @return A new instance of fragment FragmentRoute.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(/*param1: String, param2: String*/) =
            FragmentRoute().apply {
                /*arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }*/
            }
    }

    private var _binding: FragmentRouteBinding? = null
    private val binding get() = _binding!!

    private var startPlaces: ArrayList<Place> = ArrayList()
    private var routeStops: ArrayList<RouteNode> = ArrayList()
    private var routeMode: String = "foot-walking"

    private val viewModelMain: ViewModelMain by activityViewModels { Application.factory }
    private val viewModelUser: ViewModelUser by activityViewModels { Application.factory }

    private var categoryManager: ClassCategoryManager? = null
    private var resources: Resources? = null

    private lateinit var routeStopsAdapter: AdapterRouteStopsRecyclerView

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
        _binding = FragmentRouteBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryManager= ClassCategoryManager(requireContext())
        resources = getResources()

        /** [ViewModelMain.mainRouteState] observer
         *  observe the [viewModelMain]'s [ViewModelMain.mainRouteState]
         *  on state update
         *  - call [handleRouteStopsChange]
         *  - call [handleRouteNavigationChange]
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModelMain.mainRouteState.collect {

                    handleRouteStopsChange(
                        route = it.route
                    )

                    Log.d("routePolysFragment",  it.route.getRouteNodes().size.toString())

                    Log.d("refresh", "refresh")
                }

            }
        }

        routeStopsAdapter = AdapterRouteStopsRecyclerView(
            routeStops = this.routeStops,
            mode = this.routeMode
        )

        binding.routeStopsList.layoutManager = LinearLayoutManager(context)
        binding.routeStopsList.adapter = routeStopsAdapter

        val touchHelper: ItemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.Callback() {

            var drag = false

            var draggedIndex: Int = 0
            var targetIndex: Int = 0

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {

                return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN , ItemTouchHelper.END)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                draggedIndex = viewHolder.adapterPosition
                targetIndex = target.adapterPosition

                routeStopsAdapter.notifyItemMoved(draggedIndex, targetIndex);

                return true
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                when(direction) {
                    ItemTouchHelper.END -> {
                        val removed = routeStops[viewHolder.adapterPosition]
                        viewModelMain.addRemovePlaceToRoute(removed.placeUUID.toString())
                    }
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if(actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    drag = true
                    Log.d("DragTest","DRAGGGING start");
                }
                if(actionState == ItemTouchHelper.ACTION_STATE_IDLE && drag) {
                    Log.d("DragTest", "DRAGGGING stop");

                    if (draggedIndex != targetIndex) {

                        val routeNode = routeStops[draggedIndex]

                        viewModelMain.reorderRoute(
                            newPosition = targetIndex,
                            nodeToMove = routeNode
                        )
                    }
                    drag = false
                }

            }

        })

        touchHelper.attachToRecyclerView(binding.routeStopsList)

        routeStopsAdapter.setOnClickListener(object : AdapterRouteStopsRecyclerView.OnClickListener {


            override fun onClick(
                uuid: String?,
                coordinates: Coordinates?
            ) {
                /**
                 * set the current place based on the uuid of the [Place]
                 * associated with the selected [RouteNode]
                 */
                viewModelMain.getCurrentPlaceByUUID(
                    uuid = uuid!!
                )

                /**
                 * call the [updateMapOnRouteStopSelected] method
                 */
                viewModelMain.setSelectedRouteNodePosition(
                    coordinates = coordinates!!
                )

            }
        })

        /**
         * check the walk mode as the default mode for route planning
         */
        binding.routeModeGroup.check(binding.routeSelectWalk.id)

        /**
         * add an [OnButtonCheckedListener] for the route transport mode selector
         * button group
         * on check change update the transport mode
         */
        binding.routeModeGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->

            //get the currently selected index
            val index = binding.routeModeGroup.indexOfChild(binding.routeModeGroup.findViewById(binding.routeModeGroup.checkedButtonId))

            //set the route mode according to the index
            viewModelMain.setRouteTransportMode(
                index = index
            )
        }

        binding.startNavigation.setOnClickListener { l ->

            viewModelMain.startNavigationThroughPlacesInRoute()
        }

        /**
         * add an [OnclickListener] for the [dismissRoutePlan] button
         * when clicked reset the route [ViewModelMain.resetRoute]
         */
        binding.dismissRoutePlan.setOnClickListener { l ->

            viewModelMain.resetRoute()

            viewModelMain.returnToPrevContent()

            viewModelMain.resetCurrentPlace()
        }

        binding.optimizeRoute.setOnClickListener { l ->
            viewModelMain.optimizeRoute()
        }

//_________________________________________________________________________________________________________________________
// END OF ROUTE METHODS BLOCK
//_________________________________________________________________________________________________________________________
    }

    override fun onDestroyView() {
        super.onDestroyView()

        resources?.flushLayoutCache()

        _binding = null
    }

    /** Route methods block
     *  initializes the [routeStopsAdapter] with a default empty [routeStops]
     *   and the default [routeMode] that is walking
     *  sets a layout manager for the [binding.routeStopsList] recycler view
     *   and an adapter which is the initialized [routeStopsAdapter]
     *  sets an [onClickListener] for the same adapter too
     **/
//_________________________________________________________________________________________________________________________
// BEGINNING OF ROUTE METHODS BLOCK
//_________________________________________________________________________________________________________________________

    //Methods related to route
//_________________________________________________________________________________________________________________________
// BEGINNING OF METHODS FOR ROUTE
//_________________________________________________________________________________________________________________________
    /** [handleRouteStopsChange]
     * call [showRouteData] and [showHideSearchAndRouteElementsOnRouteStopsChange]
     */
    fun handleRouteStopsChange(route: Route) {

        showRouteData(
            route = route
        )
    }


    /** [showRouteData]
     * refresh the [routeStopsAdapter]'s list with the currently added places
     * set the calculated full duration of the route (by car/on foot)
     * as the text of the route mode selection buttons
     */
    private fun showRouteData(route: Route) {

        this.routeStops.clear()

        this.routeStops.addAll(route.getRouteNodes())

        this.routeMode = route.getTransportMode()

        this.routeStopsAdapter.mode = route.getTransportMode()

        routeStopsAdapter.notifyDataSetChanged()

        val fullWalkDuration = route.fullWalkDuration.toString() + " " + resources?.getString(R.string.duration_string)
        val fullCarDuration = route.fullCarDuration.toString() + " " + resources?.getString(R.string.duration_string)

        binding.routeSelectWalk.text =  fullWalkDuration
        binding.routeSelectCar.text =  fullCarDuration
    }

//_________________________________________________________________________________________________________________________
// END OF METHODS FOR ROUTE
//_________________________________________________________________________________________________________________________


}