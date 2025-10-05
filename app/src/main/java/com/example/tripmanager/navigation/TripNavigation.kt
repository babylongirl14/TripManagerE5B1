package com.example.tripmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tripmanager.ui.screens.LoginScreen
import com.example.tripmanager.ui.screens.RegisterScreen
import com.example.tripmanager.ui.screens.TripsScreen
import com.example.tripmanager.ui.screens.TripEditScreen
import com.example.tripmanager.ui.screens.TripDetailScreen
import com.example.tripmanager.ui.screens.PinScreen
import com.example.tripmanager.ui.screens.PinResetScreen
import com.example.tripmanager.ui.screens.DocumentsScreen
import com.example.tripmanager.ui.screens.DocumentEditScreen
import com.example.tripmanager.ui.screens.DocumentViewerScreen
import com.example.tripmanager.ui.screens.ChangePinScreen
import com.example.tripmanager.ui.screens.ItineraryListScreen
import com.example.tripmanager.ui.screens.ItineraryEditScreen

@Composable
fun TripNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToTrips = { username ->
                    navController.navigate(Screen.Trips.createRoute(username)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Trips.route,
            arguments = Screen.Trips.arguments
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            TripsScreen(
                username = username,
                onNavigateToAddTrip = {
                    navController.navigate(Screen.TripEdit.createRoute(null))
                },
                onNavigateToEditTrip = { tripId ->
                    navController.navigate(Screen.TripEdit.createRoute(tripId))
                },
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                }
            )
        }
        
        composable(
            route = Screen.TripEdit.route,
            arguments = Screen.TripEdit.arguments
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull()
            TripEditScreen(
                tripId = tripId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.TripDetail.route,
            arguments = Screen.TripDetail.arguments
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: 0L
            TripDetailScreen(
                tripId = tripId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDocuments = {
                    navController.navigate(Screen.PinEntry.createRoute(tripId))
                },
                onNavigateToItinerary = {
                    navController.navigate(Screen.Itinerary.createRoute(tripId))
                }
            )
        }
        
        composable(
            route = Screen.PinEntry.route,
            arguments = Screen.PinEntry.arguments
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: 0L
            PinScreen(
                tripId = tripId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPinSuccess = {
                    navController.navigate(Screen.Documents.createRoute(tripId)) {
                        popUpTo(Screen.PinEntry.route) { inclusive = true }
                    }
                },
                onNavigateToReset = {
                    navController.navigate(Screen.PinReset.route)
                }
            )
        }
        
        composable(
            route = Screen.Documents.route,
            arguments = Screen.Documents.arguments
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: 0L
            DocumentsScreen(
                tripId = tripId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddDocument = {
                    navController.navigate(Screen.DocumentEdit.createRoute(tripId, null))
                },
                onNavigateToEditDocument = { documentId ->
                    navController.navigate(Screen.DocumentEdit.createRoute(tripId, documentId))
                },
                onNavigateToDocumentViewer = { documentId ->
                    navController.navigate("document_viewer/$documentId")
                },
                onNavigateToPinSettings = {
                    navController.navigate(Screen.ChangePin.route)
                }
            )
        }
        
        composable(
            route = Screen.DocumentEdit.route,
            arguments = Screen.DocumentEdit.arguments
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: 0L
            val documentId = backStackEntry.arguments?.getString("documentId")?.toLongOrNull()
            DocumentEditScreen(
                tripId = tripId,
                documentId = documentId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("document_viewer/{documentId}") { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId")?.toLongOrNull() ?: 0L
            DocumentViewerScreen(
                documentId = documentId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.PinReset.route) {
            PinResetScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPinResetSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ChangePin.route) {
            ChangePinScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPinChangeSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Itinerary.route,
            arguments = Screen.Itinerary.arguments
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: 0L
            ItineraryListScreen(
                tripId = tripId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddItinerary = {
                    navController.navigate(Screen.ItineraryEdit.createRoute(tripId, null))
                },
                onNavigateToEditItinerary = { itineraryId ->
                    navController.navigate(Screen.ItineraryEdit.createRoute(tripId, itineraryId))
                }
            )
        }
        
        composable(
            route = Screen.ItineraryEdit.route,
            arguments = Screen.ItineraryEdit.arguments
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: 0L
            val itineraryId = backStackEntry.arguments?.getString("itineraryId")?.toLongOrNull()
            ItineraryEditScreen(
                tripId = tripId,
                itineraryId = itineraryId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
