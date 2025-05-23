package com.example.travel_mate

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.travel_mate.databinding.FragmentSearchBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
/*private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"*/

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentSearch.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentSearch : Fragment() {
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
         * @return A new instance of fragment FragmentSearch.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(/*param1: String, param2: String*/) =
            FragmentSearch().apply {
                /*arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }*/
            }
    }
    data class ChipCategory(val id: Int?,val content: String,val icon: Int?,val title: Int?,var checked: Boolean?,var category: String)

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    /*private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase*/

    private val chipGroupCategories: Map<Int, List<ChipCategory>> = mapOf(
        0 to listOf(
            ChipCategory(1,"\"amenity\"=\"restaurant\"",R.drawable.ic_restaurant,R.string.restaurant, false,"restaurant"),
            ChipCategory(2,"\"amenity\"=\"cafe\"", R.drawable.ic_cafe,R.string.cafe,false,"cafe"),
            ChipCategory(3,"\"amenity\"=\"fast_food\"",R.drawable.ic_fast_food,R.string.fast_food, false,"fast_food"),
            ChipCategory(4,"\"amenity\"=\"pub\";\"amenity\"=\"bar\"",R.drawable.ic_bar,R.string.pub_bar,false,"pub_bar")
        ),
        3 to listOf(
            ChipCategory(1,"\"amenity\"=\"cinema\"", R.drawable.ic_cinema,R.string.cinema, false,"cinema"),
            ChipCategory(2,"\"amenity\"=\"theatre\"", R.drawable.ic_theatre,R.string.theatre, false,"theatre"),
            ChipCategory(3,"\"amenity\"=\"nightclub\"", R.drawable.ic_nightclub, R.string.nightclub, false,"nightclub"),
            ChipCategory(4,"\"amenity\"=\"casino\"",R.drawable.ic_casino, R.string.casino, false,"casino")
        ), 5 to listOf(
            ChipCategory(1,"\"tourism\"=\"theme_park\"",R.drawable.ic_theme_park,R.string.adventure_park,false,"theme_park"),
            ChipCategory(2,"\"leisure\"=\"water_park\"", R.drawable.ic_beach_resort,R.string.spa,false,"water_park"),
            ChipCategory(3,"\"leisure\"=\"beach_resort\"", R.drawable.ic_beach,R.string.beach_resort,false,"beach_resort"),
            ChipCategory(4,"\"tourism\"=\"zoo\"",R.drawable.ic_zoo,R.string.zoo,false,"zoo")
        ))

    private val landmarkChip: ChipCategory = ChipCategory(null,"\"historic\"=\"memorial\";" +
            "\"building\"=\"cathedral\";" +
            "\"amenity\"=\"place_of_worship\";" +
            "\"amenity\"=\"monastery\";" +
            "\"historic\"=\"building\";" +
            "\"building\"=\"basilica\";" +
            "\"historic\"=\"monument\";" +
            "\"building\"=\"church\";" +
            "\"building\"=\"temple\";" +
            "\"castle_type\"=\"palace\";" +
            "\"historic\"=\"fort\";" +
            "\"historic\"=\"castle\"" //;+ add
        ,null,null,null,"landmark")

    private val accommodationChip: ChipCategory = ChipCategory(null,"\"building\"=\"hotel\";" +
            "\"leisure\"=\"summer_camp\";" +
            "\"tourism\"=\"caravan_site\";" +
            "\"tourism\"=\"hostel\";" +
            "\"tourism\"=\"motel\";" +
            "\"tourism\"=\"guest_house\";" +
            "\"tourism\"=\"camp_site\"" //; + add
        ,null,null,null,"accommodation")
    private val exhibitionChip: ChipCategory = ChipCategory(null,"\"tourism\"=\"museum\";" +
            "\"tourism\"=\"gallery\";" +
            "\"amenity\"=\"arts_centre\";" +
            "\"amenity\"=\"exhibition_centre\"" //; + add
        ,null,null,null,"exhibition")

    private var chipGroupCategory: List<ChipCategory> = emptyList()

    private lateinit var suggestionsAdapter: AdapterSuggestion

    private var categoryManager: ClassCategoryManager? = null
    private var resources: Resources? = null

    private val viewModelMain: ViewModelMain by activityViewModels { Application.factory }
    private val viewModelUser: ViewModelUser by activityViewModels { Application.factory }

    private lateinit var popupView: View

    private var editingInspectTrip: Boolean = false

    var startPlace: Place = Place()
    private var startPlaces: ArrayList<Place> = ArrayList()
    private var suggestions: ArrayList<String> = ArrayList()

    private lateinit var dialog: Dialog

    var dist: Double = 0.0

    /**
     * search for potential starting places based on the input text
     * if the length is longer or equal to 4
     * */
    private val textWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            if (s.length >= 4) {

                viewModelMain.searchAutocomplete(s.toString())
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

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
        _binding = FragmentSearchBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inflater = LayoutInflater.from(context)
        popupView = inflater.inflate(R.layout.layout_popup_menu,null)

        resources = getResources()

        dialog = Dialog(popupView.context)
        dialog.setContentView(popupView)
        dialog.setCancelable(false)

        /** [ViewModelMain.chipsState] observer
         *  observe the [viewModelMain]'s [ViewModelMain.chipsState]
         *  on state update
         *  - the [createChipGroupDialog] function is called
         *   if the currentChipGroup and the currentChipGroupContent != null
         *   else dismiss the [dialog]
         *  - [showHideExtendedSearch] function is called
         *  - [enableDisableMinuteSelect] function is called
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModelMain.chipsState.collect {

                    /*if (it.currentPlace != null)
                        updateCurrentPlace(it.currentPlace)*/
                    if (it.currentChipGroup != null && it.currentChipGroupContent != null) {

                        //chipGroupCategory = it.currentChipGroupContent

                        createChipGroupDialog(
                            id = it.currentChipGroup,
                            chipGroupContent = it.currentChipGroupContent
                        )

                    } else {

                        dismissDialog()
                    }

                    showHideExtendedSearch(it.showExtendedSearch)

                    enableDisableMinuteSelect(it.transportMode)

                    dist = it.distance

                    Log.d("refresh", "refresh")
                }

            }
        }

        /** [ViewModelMain.mainSearchState] observer
         *  observe the [viewModelMain]'s [ViewModelMain.mainSearchState]
         *  on state update
         *  - the [handleStartPlaceChange] function is called
         *  - [handlePhotonObserve] function is called
         *   if its [startPlaces] is not empty
         *  - [showMapContent] function is called
         *  - [handleRouteStopsChange] function is called
         *  - [ViewModelMain.getCurrentPlaceByUUID] function is called
         *   if there is a currentPlace selected
         *
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelMain.mainSearchState.collect {

                    Log.d("isStartPlacesEmpty", it.startPlaces.isEmpty().toString())

                    if (it.startPlaces.isNotEmpty())
                        handlePhotonObserve(
                            places = it.startPlaces
                        )

                    if (it.currentPlaceUUID != null)
                        viewModelMain.getCurrentPlaceByUUID(
                            uuid = it.currentPlaceUUID
                        )

                    handleTripButtons(
                        editingInspectTrip = editingInspectTrip,
                        isTripPlacesEmpty = it.isTripEmpty
                    )

                    Log.d("tripEmpty", it.isTripEmpty.toString())
                }
            }
        }

        /** [ViewModelMain.mainStartPlaceState] observer
         *  observe the [viewModelMain]'s [ViewModelMain.mainStartPlaceState]
         *  on state update
         *  - call [handleStartPlaceChange]
         */

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModelMain.mainStartPlaceState.collect {

                    handleStartPlaceChange(
                        startPlace = it.startPlace
                    )

                    Log.d("refresh", "refresh")
                }

            }
        }

        /** [ViewModelMain.mainInspectTripState] observer
         *  observe the [viewModelMain]'s [ViewModelMain.mainInspectTripState]
         *  on state update
         *  - call [enableDisableNavigateToUserFragment]
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModelMain.mainInspectTripState.collect {

                    editingInspectTrip = it.editing

                    enableDisableNavigateToUserFragment(
                        editing = it.editing
                    )

                    handleInspectTripButtons(
                        editing = it.editing
                    )

                    Log.d("editing", it.editing.toString())
                }
            }
        }

        /**Listener to handle CLICKING THE USER BUTTON
         * navigate to the [FragmentUser]
         * */
//_________________________________________________________________________________________________________________________
// BEGINNING OF USER BUTTON LISTENER
// _________________________________________________________________________________________________________________________
        binding.userBTN2.setOnClickListener{ v ->
            animateImageButton(v)

            findNavController().navigate(R.id.action_FragmentMain_to_FragmentUser)
        }
//_________________________________________________________________________________________________________________________
// END OF USER BUTTON LISTENER
// _________________________________________________________________________________________________________________________

        /**Listeners to handle searching start place
         * initialize the [suggestionsAdapter] adapter
         * set this adapter for the [placeSearch] [MaterialAutoCompleteTextView]
         * sets the background of the same text view
         * create an [OnItemClickListener] too
         */
//_________________________________________________________________________________________________________________________
// BEGINNING OF PLACE SEARCH LISTENERS
// _________________________________________________________________________________________________________________________

        suggestionsAdapter = AdapterSuggestion(
            context =  requireContext(),
            resource = R.layout.layout_autocomplete_item ,
            objects = suggestions
        )

        binding.placeSearch.setAdapter(suggestionsAdapter)

        binding.placeSearch.setDropDownBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.shape_autocomplete_dropdown))

        /*
        * initialize a new search
         * hide the soft input keyboard
         */
        binding.placeSearch.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->

                viewModelMain.initNewSearch(startPlaces[position])

                viewModelMain.resetStartPlaces()

                clearChips()

                dismissDialog()

                Log.d("viewModelStartPlaceTest1", startPlaces[position].getName()!!)

                /*binding.placeSearch.setText(suggestions[position])*/

                val inputMethodManager: InputMethodManager =
                    getSystemService(requireContext(),InputMethodManager::class.java) as InputMethodManager
                val currentFocusView = activity?.currentFocus
                currentFocusView?.let {
                    inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                }
                binding.placeSearch.clearFocus()

            }
        // set the threshold as 4 for the search text view
        binding.placeSearch.threshold = 4

        binding.placeSearch.addTextChangedListener(textWatcher)

//_________________________________________________________________________________________________________________________
// END OF PLACE SEARCH LISTENERS
//_________________________________________________________________________________________________________________________

//Listeners to handle extended search
//_________________________________________________________________________________________________________________________
// BEGINNING OF EXTENDED SEARCH LISTENERS
//_________________________________________________________________________________________________________________________

        /**
         * if the "extended search" button is checked set the extendedSearchVisible state attribute as true
         * in [ViewModelMain]
         * else reset the extended search attributes which are the selected transport mode and the distance in minutes
         */
        binding.openExtended.addOnCheckedChangeListener { button, isChecked ->

            if (isChecked) {

                viewModelMain.setExtendedSearchVisible(true)

            } else {

                if (dist != 0.0) {

                    viewModelMain.resetDetails()

                }
                uncheckExtendedSearch()

                clearChips()

                dismissDialog()

                viewModelMain.resetExtendedSearch()
            }
        }
        /**
         * transport mode selection listener
         * set the transport mode as selected
         */
        binding.transportGroup.addOnButtonCheckedListener{ group, checkedId, isChecked ->

            val index = binding.transportGroup.indexOfChild(binding.transportGroup.findViewById(binding.transportGroup.checkedButtonId))

            viewModelMain.setTransportMode(index)
        }
        /**
         * distance selection listener
         * set the distance that is in minutes as selected
         */
        binding.distanceGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->

            val index = binding.distanceGroup.indexOfChild(binding.distanceGroup.findViewById(binding.distanceGroup.checkedButtonId))

            viewModelMain.setMinute(index)

            viewModelMain.setExtendedSearchSelected(true)
        }
//_________________________________________________________________________________________________________________________
// END OF EXTENDED SEARCH LISTENERS
//_________________________________________________________________________________________________________________________

//Listener to handle using the current location for start place
//_________________________________________________________________________________________________________________________
// BEGINNING OF CURRENT LOCATION LISTENERS
//_________________________________________________________________________________________________________________________

        /**
         * location button listener
         * if it is checked, ask for current location updates
         * then with the received coordinates, make a reverseGeoCode request to the [PhotonRemoteDataSource]
         * if it is unchecked reset the ui, the search
         * preparing it for an other location selection or an input to the search field
         */
        binding.useLocation.addOnCheckedChangeListener{ l, isChecked ->

            viewModelMain.resetFullDetails()

            if (isChecked) {

                viewModelMain.searchReverseGeoCode()

                binding.placeSearch.isEnabled = false
            } else {

                binding.placeSearch.isEnabled = true
            }
        }
//_________________________________________________________________________________________________________________________
// END OF CURRENT LOCATION LISTENERS
//_________________________________________________________________________________________________________________________

//Listeners for chips used to search nearby relevant places by specific categories
//_________________________________________________________________________________________________________________________
// BEGINNING OF CHIP LISTENERS
//_________________________________________________________________________________________________________________________
        /**
         * if the scrollView containing the category chips
         * are scrolled call the [ViewModelMain.resetCurrentChipGroup] function
         */
        binding.chipGroups.setOnScrollChangeListener{ _, _, _, _, _ ->

            viewModelMain.resetCurrentChipGroup()
        }

        /**
         * add a listener for each chip in the category chips [android.widget.HorizontalScrollView]
         * in [fragment_main.xml]
         *-  on select if the selected chip is not a group chip
         * make a search for the places with the category that specific chip represents
         * - if it is a group chip update the dialog that contains the chips that represent
         * the categories defined in that group
         * - else, on uncheck remove the markers based on the category associated with them
         */
        for (groupChip in binding.chipGroupChips.children){

            if (groupChip is Chip) {

                groupChip.setOnCheckedChangeListener{ l, isChecked ->

                    val groupIndex = binding.chipGroupChips.indexOfChild(binding.chipGroupChips.findViewById(l.id))

                    if (groupIndex in chipGroupCategories.keys){

                        if (!isChecked){

                            viewModelMain.resetCurrentChipGroup()

                        }else{
                            Log.d("check", "check")

                            val chipGroupContent = chipGroupCategories[groupIndex]

                            if (chipGroupContent != null) {
                                viewModelMain.setCurrentChipGroup(l.id,chipGroupContent)
                            }
                        }


                    }else if (groupIndex == 1){

                        handleCategoryFiltererChipCheckChange(accommodationChip.content,accommodationChip.category, isChecked/*, l.id*/)
                    }else if (groupIndex == 2){

                        handleCategoryFiltererChipCheckChange(exhibitionChip.content,exhibitionChip.category, isChecked/*, l.id*/)

                    }else {

                        handleCategoryFiltererChipCheckChange(landmarkChip.content,landmarkChip.category, isChecked/*, l.id*/)

                    }


                }
            }
        }
//_________________________________________________________________________________________________________________________
// END OF CHIP LISTENERS
//_________________________________________________________________________________________________________________________

    }

    override fun onDestroyView() {
        super.onDestroyView()

        dismissDialog()

        binding.distanceGroup.clearOnButtonCheckedListeners()
        binding.transportGroup.clearOnButtonCheckedListeners()

        resources?.flushLayoutCache()

        _binding = null
    }

    //Methods related to the search text field
//_________________________________________________________________________________________________________________________
// BEGINNING OF METHODS FOR SEARCH TEXT FIELD
//_________________________________________________________________________________________________________________________

    /** [handleStartPlaceChange]
     *  on start place change in the [ViewModelMain] caused by an update in [SearchRepository]
     *  first remove all content from the map
     *  then call [showStart], [setStartTextFiled]
     *  and set the visibility of the category chips.
     */

    private fun handleStartPlaceChange(startPlace: Place?) {

        resetUiOnStartPlaceChange()

        setStartTextFiled(
            startPlace = startPlace
        )

        if (startPlace != null) {

            binding.chipGroupChips.visibility = View.VISIBLE
        } else {

            binding.chipGroupChips.visibility = View.GONE
        }
    }

    /** [setStartTextFiled]
     * set the search field's hint as the current start place's name + address
     * on search place change from [ViewModelMain] and [SearchRepository]
     */
    private fun setStartTextFiled(startPlace: Place?) {

        if (startPlace != null) {

            this.startPlace = startPlace

            resetSearchField()

            val stringBuilder = StringBuilder()

            stringBuilder.append(startPlace.getName() + " ")
            stringBuilder.append(startPlace.getAddress()?.getFullAddress())

            binding.placeSearch.setText("")
            binding.placeSearch.hint = stringBuilder.toString()

        } else {

            resetSearchField()
        }
    }

    /** [resetSearchField]
     * set the default hint for the search field
     */
    private fun resetSearchField() {

        binding.placeSearch.hint = resources?.getString(R.string.search_place)
        binding.placeSearch.setText("")
    }

    /** [handlePhotonObserve]
     *  handle the potential start places change received from the autocomplete service
     *  refresh the text view's adapter with the name + address string received
     */
    private fun handlePhotonObserve(places: List<Place>){

        startPlaces.clear()
        suggestions.clear()

        startPlaces.addAll(places)

        suggestions.addAll( startPlaces.map {

            val suggestion = StringBuilder()
            suggestion.append(it.getName()).append(" ")
            suggestion.append(it.getAddress()?.getFullAddress() ?: "")

            suggestion.toString()
        })

        suggestionsAdapter = AdapterSuggestion(
            context =  requireContext(),
            resource = R.layout.layout_autocomplete_item ,
            objects = suggestions
        )

        binding.placeSearch.setAdapter(suggestionsAdapter)

        suggestionsAdapter.notifyDataSetChanged()
    }

    /** [resetUiOnStartPlaceChange]
     * reset the UI elements related to searching when the start[Place] is changed
     */
    private fun resetUiOnStartPlaceChange(){

        clearChips()

        dismissDialog()
    }
//_________________________________________________________________________________________________________________________
// END OF METHODS FOR SEARCH TEXT FIELD
//_________________________________________________________________________________________________________________________


//Methods related to the extended search
//_________________________________________________________________________________________________________________________
// BEGINNING OF METHODS FOR EXTENDED SEARCH
//_________________________________________________________________________________________________________________________
    /** [showHideExtendedSearch]
     *  show or hide the extended search buttons based on the current value of the
     *  [viewModelMain]'s state attributes
     */
    private fun showHideExtendedSearch(showExtendedSearch: Boolean) {

        if (showExtendedSearch) {

            binding.transportGroup.visibility = View.VISIBLE
            binding.distanceGroup.visibility = View.VISIBLE

            binding.chipGroups.visibility = View.GONE

        }else {

            binding.transportGroup.visibility = View.GONE
            binding.distanceGroup.visibility = View.GONE

            binding.chipGroups.visibility = View.VISIBLE
        }

        dismissDialog()
    }

    private fun enableDisableMinuteSelect(transportMode: String?) {

        val hasChecked = transportMode != null

        if (hasChecked){
            binding.distanceGroup.isEnabled = true
        } else {
            binding.distanceGroup.clearChecked()
            binding.distanceGroup.isEnabled = false
        }
    }

    private fun uncheckExtendedSearch() {

        binding.transportGroup.clearChecked()
        binding.distanceGroup.clearChecked()
        binding.distanceGroup.isEnabled = false
    }
//_________________________________________________________________________________________________________________________
// END OF METHODS FOR EXTENDED SEARCH
//_________________________________________________________________________________________________________________________

//Methods related to the category chips
//_________________________________________________________________________________________________________________________
// BEGINNING OF METHODS FOR CATEGORY CHIPS
//_________________________________________________________________________________________________________________________
    /** [createChipGroupDialog]
     *  create the customised [Dialog] that contains the search categories of that
     *  specific group
     */
    private fun createChipGroupDialog(id: Int, chipGroupContent :List<ChipCategory>){

        val groupChip: Chip = binding.chipGroupChips.findViewById(id)

        groupChip.post {
            // Get chip position on screen

            val location = IntArray(2)
            groupChip.getLocationOnScreen(location)
            val chipHeight = groupChip.height * 0.5

            // Position the dialog under the chip
            dialog.window?.apply {
                val params = this.attributes
                params.gravity = Gravity.TOP or Gravity.START
                params.x = location[0] // Set X position to chip's left edge
                params.y = (location[1]+chipHeight).toInt() // Set Y position just below the chip
                this.attributes = params
                setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                setBackgroundDrawable(ResourcesCompat.getDrawable(/*fragmentMain.*/resources!!,R.color.transparent, context.theme))
                setDimAmount(0f)
            }

            val layout = popupView.findViewById<LinearLayout>(R.id.popup_menu_content)

            layout.removeAllViews()

            for (content in chipGroupContent){

                val chip = createSearchChip(content)

                layout.addView(chip)

            }

            dialog.show()
        }
    }

    private fun dismissDialog() {
        dialog.dismiss()
    }

    /** [createSearchChip]
     * create a chip that will be added to the dialog's layout
     * these chips work the same way as the ones not part of a group
     */
    private fun createSearchChip(chipCategory: ChipCategory): Chip{
        val chip = Chip(context,null,R.style.material3_Chip).apply {

            this.id = chipCategory.id!!
            this.chipIcon = ResourcesCompat.getDrawable(resources,chipCategory.icon!!, context.theme)
            this.text = resources.getString(chipCategory.title!!)
            this.isClickable = true
            this.isCheckable = true
            this.isChecked = chipCategory.checked!!
            val cornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8f, // 8dp
                resources.displayMetrics
            )
            this.chipCornerRadius = cornerRadius
            isChipIconVisible = !chipCategory.checked!!
        }

        chip.setOnCheckedChangeListener{ _, isChecked ->

            chip.isChipIconVisible = !isChecked/*

            val index = chipGroupCategory.indexOf(chipCategory)*/

            if(!isChecked){

                //chipGroupCategory[index].checked = false
                chipCategory.checked = false

                handleCategoryFiltererChipCheckChange(chipCategory.content,chipCategory.category,
                    chipCategory.checked!!
                )
            }else{
                //chipGroupCategory[index].checked = true
                chipCategory.checked = true

                handleCategoryFiltererChipCheckChange(chipCategory.content,chipCategory.category,
                    chipCategory.checked!!)
            }/*
            Log.d("ischipchecked", chipGroupCategory[index].checked.toString())
            viewModelMain.setCurrentChipGroupContent(chipGroupCategory)*/

        }
        return chip
    }

    private fun handleCategoryFiltererChipCheckChange(content: String, category: String, checked: Boolean, id: Int? = null){

        if (checked){
            handleCategoryFilterChipChecked(content,category)
            if (id != null)
                viewModelMain.addSelectedChip(id)
        }else{
            handleCategoryFilterChipUnchecked(category)
            if (id != null)
                viewModelMain.removeSelectedChip(id)
        }

    }

    private fun handleCategoryFilterChipChecked(content: String, category: String){

        searchChipCategory(content,category)
    }

    /** [handleCategoryFilterChipUnchecked]
     * remove the places with the category given in parameter
     */
    private fun handleCategoryFilterChipUnchecked(category: String){

        viewModelMain.removePlacesByCategory(category)
    }

    /** [searchChipCategory]
     *  make a search by the selected category and the current start place's coordinates
     */
    private fun searchChipCategory(content: String, category: String){

        viewModelMain.searchOverpass(
            content,
            startPlace.getCoordinates().getLatitude().toString(),
            startPlace.getCoordinates().getLongitude().toString(),
            startPlace.getAddress()?.getCity()!!,
            category
        )
    }

    /** [clearChips]
     *  reset the selection of the selected category chips
     */
    private fun clearChips() {

        chipGroupCategories.values.flatten().forEach { it.checked = false }

        listOf(accommodationChip, landmarkChip, exhibitionChip).forEach { it.checked = false }

        binding.chipGroupChips.clearCheck()
    }
//_________________________________________________________________________________________________________________________
// END OF METHODS FOR CATEGORY CHIPS
//_________________________________________________________________________________________________________________________

    private fun animateImageButton(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.image_button_click)
        view.startAnimation(animation)
    }

    private fun handleInspectTripButtons(editing: Boolean){

        when (editing){
            false -> {
                removeInspectTripButtonListeners()
            }
            true -> {
                setupInspectTripButtonListeners()
            }
        }
        handleInspectTripUiChanges(editing)

    }

    private fun handleTripButtons(editingInspectTrip: Boolean, isTripPlacesEmpty: Boolean){

        when (isTripPlacesEmpty || editingInspectTrip){
            true -> {
                removeTripButtonListeners()
            }
            else -> {
                setupTripButtonListeners()
            }
        }
        handleTripUiChanges(editingInspectTrip,isTripPlacesEmpty)

    }

    //Methods for the buttons responsible for opening the fragment for saving or sharing a trip
//_________________________________________________________________________________________________________________________
// BEGINNING OF TRIP METHODS
//_________________________________________________________________________________________________________________________

    private fun enableDisableNavigateToUserFragment(editing: Boolean) {

        binding.userBTN2.isEnabled = !editing
    }

    private fun setupInspectTripButtonListeners() {

        binding.saveInspectTrip.setOnClickListener { l ->

            viewModelUser.saveTripWithUpdatedPlaces(
                startPlace = viewModelMain.getStartPlace()!!,
                places = viewModelMain.getPlacesContainedByTrip()
            )
        }

        binding.dismissInspectTrip.setOnClickListener { l ->

            viewModelMain.cancelEditInspected()
        }
    }

    private fun removeInspectTripButtonListeners() {

        binding.saveInspectTrip.setOnClickListener(null)
        binding.dismissInspectTrip.setOnClickListener(null)
    }

    /**
     *
     */
    private fun setupTripButtonListeners() {

        binding.saveTrip.setOnClickListener { l ->

            viewModelUser.setUpdatedFrom("main")

            viewModelUser.initAddUpdateTrip(
                startPlace = viewModelMain.getStartPlace()!!,
                places = viewModelMain.getPlacesContainedByTrip()
            )
            findNavController().navigate(R.id.action_FragmentMain_to_fragmentSaveTrip)
        }
        binding.dismissTrip.setOnClickListener { l ->

            viewModelMain.clearPlacesAddedToTrip()
        }
    }

    private fun removeTripButtonListeners() {

        binding.saveTrip.setOnClickListener(null)
        binding.dismissTrip.setOnClickListener(null)
    }

    private fun handleInspectTripUiChanges(editing: Boolean){

        when(editing) {
            true -> {
                binding.saveInspectTrip.setVisibility(View.VISIBLE)
                binding.dismissInspectTrip.setVisibility(View.VISIBLE)
            }
            false -> {
                binding.saveInspectTrip.setVisibility(View.GONE)
                binding.dismissInspectTrip.setVisibility(View.GONE)
            }
        }

    }

    private fun handleTripUiChanges(editingInspectTrip: Boolean, isEmpty: Boolean){

        binding.saveTrip.setVisibility(View.GONE)
        binding.dismissTrip.setVisibility(View.GONE)

        if (!isEmpty && !editingInspectTrip) {

            binding.saveTrip.setVisibility(View.VISIBLE)
            binding.dismissTrip.setVisibility(View.VISIBLE)
        }
    }

//_________________________________________________________________________________________________________________________
// END OF TRIP METHODS
//_________________________________________________________________________________________________________________________
}