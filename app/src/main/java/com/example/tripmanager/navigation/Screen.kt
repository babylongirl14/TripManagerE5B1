package com.example.tripmanager.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Login : Screen("login")
    
    object Register : Screen("register")
    
    object Trips : Screen(
        route = "trips/{username}",
        arguments = listOf(
            navArgument("username") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(username: String) = "trips/$username"
    }
    
    object TripEdit : Screen(
        route = "trip_edit/{tripId}",
        arguments = listOf(
            navArgument("tripId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        fun createRoute(tripId: Long?) = "trip_edit/${tripId ?: "new"}"
    }
    
    object TripDetail : Screen(
        route = "trip_detail/{tripId}",
        arguments = listOf(
            navArgument("tripId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(tripId: Long) = "trip_detail/$tripId"
    }
    
    object PinEntry : Screen(
        route = "pin_entry/{tripId}",
        arguments = listOf(
            navArgument("tripId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(tripId: Long) = "pin_entry/$tripId"
    }
    
    object PinReset : Screen("pin_reset")
    
    object ChangePin : Screen("change_pin")
    
    object Documents : Screen(
        route = "documents/{tripId}",
        arguments = listOf(
            navArgument("tripId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(tripId: Long) = "documents/$tripId"
    }
    
    object DocumentEdit : Screen(
        route = "document_edit/{tripId}/{documentId}",
        arguments = listOf(
            navArgument("tripId") {
                type = NavType.StringType
            },
            navArgument("documentId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        fun createRoute(tripId: Long, documentId: Long?) = "document_edit/$tripId/${documentId ?: "new"}"
    }
    
    object Itinerary : Screen(
        route = "itinerary/{tripId}",
        arguments = listOf(
            navArgument("tripId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(tripId: Long) = "itinerary/$tripId"
    }
    
    object ItineraryEdit : Screen(
        route = "itinerary_edit/{tripId}/{itineraryId}",
        arguments = listOf(
            navArgument("tripId") {
                type = NavType.StringType
            },
            navArgument("itineraryId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        fun createRoute(tripId: Long, itineraryId: Long?) = "itinerary_edit/$tripId/${itineraryId ?: "new"}"
    }
}
