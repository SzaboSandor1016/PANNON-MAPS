package com.example.gtk_maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelMain: ViewModel() {

    private val _startPlace = MutableLiveData<ClassPlace?>()
    val startPlace: LiveData<ClassPlace?> = _startPlace

    private val _places = MutableLiveData<ArrayList<ClassPlace>>()
    val places: LiveData<ArrayList<ClassPlace>> = _places

    private val _transportMode = MutableLiveData<String?>()
    val transportMode: LiveData<String?> = _transportMode

    private val _minute = MutableLiveData<Int?>()
    val minute: LiveData<Int?> = _minute

    private val search = ClassSearch()

    fun getCurrentSearch(): ClassSearch = search

    fun setStartPlace(startPlace: ClassPlace){

        search.setStartPlace(startPlace).also {
            _startPlace.postValue(startPlace)
        }
    }

    fun getStartPlace(): ClassPlace?{

        return search.getStartPlace()
    }

    fun setPlaces(places: ArrayList<ClassPlace>){

        search.setPlaces(places).also{

            _places.postValue(places)
        }
    }

    fun getPlaces(): ArrayList<ClassPlace>{

        return search.getPlaces()
    }

    fun addPlace(place: ClassPlace){

        search.addPlace(place)
        _places.postValue(search.getPlaces())
    }

    fun addPlaces(places: ArrayList<ClassPlace>){

        search.addPlaces(places)
        _places.postValue(search.getPlaces())
        /*_places.value = search.getPlaces()*/

        /*Log.d("test", _places.value!![0].getName()!!)*/

    }

    fun removePlace(place: ClassPlace){

        search.removePlace(place)
        _places.postValue(search.getPlaces())
    }

    fun removePlaces(places: ArrayList<ClassPlace>){

        search.removePlaces(places)
        _places.postValue(search.getPlaces())
    }

    fun setTransportMode(optionIndex: Int){

        val mode = when (optionIndex) {
            0 -> "walk" // walk
            1 -> "car" // car
            else -> null

        }

        search.setTransportMode(mode)
        _transportMode.postValue(mode)
    }

    fun getTransportMode(): String?{
        return search.getTransportMode()
    }

    fun setMinute(optionIndex: Int){

        val minute = when (optionIndex) {
            0 -> 15
            1 -> 30
            2 -> 45
            else -> null

        }

        search.setMinute(minute)

        _minute.postValue(minute)
    }

    fun getMinute(): Int?{

        return search.getMinute()
    }

    fun calculateDistance(){

        search.calculateDistance()
    }

    fun getCalculatedDistance(): Double?{

        return search.getDistance()
    }

    fun resetSearchDetails(){

        search.resetSearchDetails(). also {

            _minute.postValue(null)
            _transportMode.postValue(null)
            _places.postValue(ArrayList())
        }
    }

    fun removePlacesByCategory(category: String){

        search.removePlacesByCategory(category)
        _places.postValue(search.getPlaces())
    }

    /*fun searchAutocomplete(query: String) {

        viewModelScope.launch {

            val startPlaces = startPlacesRepository.searchAutocomplete(query)

            _startPlaces.postValue(startPlaces)
        }
    }

    fun searchReverseGeoCode(coordinates: ClassCoordinates){

        viewModelScope.launch {

            val reverseGeoCode = startPlacesRepository.searchReverseGeoCode(coordinates)

            _startPlaces.postValue(reverseGeoCode)
        }
    }

    fun searchOverpass(query: String, category: String) {

        viewModelScope.launch {

            val places = placesRepository.searchOverpass(query,category)

            addPlaces(places)
        }
    }*/


}