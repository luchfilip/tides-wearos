package dev.tidesapp.wearos.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.tidesapp.wearos.auth.ui.login.LoginScreen
import dev.tidesapp.wearos.library.ui.albumdetail.AlbumDetailScreen
import dev.tidesapp.wearos.library.ui.albums.AlbumsScreen
import dev.tidesapp.wearos.library.ui.home.HomeScreen
import dev.tidesapp.wearos.library.ui.playlistdetail.PlaylistDetailScreen
import dev.tidesapp.wearos.library.ui.playlists.PlaylistsScreen
import dev.tidesapp.wearos.library.ui.search.SearchScreen
import dev.tidesapp.wearos.player.ui.nowplaying.NowPlayingScreen
import dev.tidesapp.wearos.settings.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

// Type-safe route objects (kept for future Wear Compose Navigation type-safe support)
@Serializable object LoginRoute
@Serializable object LibraryHomeRoute
@Serializable data class AlbumDetailRoute(val albumId: String)
@Serializable data class PlaylistDetailRoute(val playlistId: String)
@Serializable object SearchRoute
@Serializable object NowPlayingRoute
@Serializable object DownloadsRoute
@Serializable object SettingsRoute

object Routes {
    const val LOGIN = "login"
    const val LIBRARY_HOME = "library_home"
    const val ALBUMS = "albums"
    const val PLAYLISTS = "playlists"
    const val ALBUM_DETAIL = "album_detail/{albumId}"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}"
    const val SEARCH = "search"
    const val NOW_PLAYING = "now_playing"
    const val DOWNLOADS = "downloads"
    const val SETTINGS = "settings"

    fun albumDetail(albumId: String) = "album_detail/$albumId"
    fun playlistDetail(playlistId: String) = "playlist_detail/$playlistId"
    fun nowPlaying(trackId: String? = null): String =
        if (trackId != null) "now_playing?trackId=$trackId" else "now_playing"
}

@Composable
fun TidesNavGraph() {
    val navController = rememberSwipeDismissableNavController()
    val navGraphViewModel: NavGraphViewModel = hiltViewModel()
    val navGraphUiState by navGraphViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (navGraphUiState is NavGraphUiState.Initial) {
            navGraphViewModel.onEvent(NavGraphUiEvent.CheckAuthState)
        }
    }

    LaunchedEffect(Unit) {
        navGraphViewModel.uiEffect.collect { effect ->
            when (effect) {
                NavGraphUiEffect.NavigateToHome -> {
                    navController.navigate(Routes.LIBRARY_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
                NavGraphUiEffect.NavigateToLogin -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.LIBRARY_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LIBRARY_HOME) {
            HomeScreen(
                onNavigateToAlbum = { albumId ->
                    navController.navigate(Routes.albumDetail(albumId))
                },
                onNavigateToPlaylist = { playlistId ->
                    navController.navigate(Routes.playlistDetail(playlistId))
                },
                onNavigateToNowPlaying = {
                    navController.navigate(Routes.nowPlaying())
                },
                onNavigateToSearch = {
                    navController.navigate(Routes.SEARCH)
                },
                onNavigateToDownloads = {
                    navController.navigate(Routes.DOWNLOADS)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
            )
        }

        composable(Routes.ALBUMS) {
            AlbumsScreen(
                onNavigateToAlbumDetail = { albumId ->
                    navController.navigate(Routes.albumDetail(albumId))
                },
            )
        }

        composable(Routes.PLAYLISTS) {
            PlaylistsScreen(
                onNavigateToPlaylistDetail = { playlistId ->
                    navController.navigate(Routes.playlistDetail(playlistId))
                },
            )
        }

        composable(
            route = Routes.ALBUM_DETAIL,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId").orEmpty()
            AlbumDetailScreen(
                albumId = albumId,
                onNavigateToNowPlaying = { trackId ->
                    navController.navigate(Routes.nowPlaying(trackId))
                },
            )
        }

        composable(
            route = Routes.PLAYLIST_DETAIL,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId").orEmpty()
            PlaylistDetailScreen(
                playlistId = playlistId,
                onNavigateToNowPlaying = { trackId ->
                    navController.navigate(Routes.nowPlaying(trackId))
                },
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onNavigateToAlbumDetail = { albumId ->
                    navController.navigate(Routes.albumDetail(albumId))
                },
                onNavigateToPlaylistDetail = { playlistId ->
                    navController.navigate(Routes.playlistDetail(playlistId))
                },
                onNavigateToNowPlaying = { trackId ->
                    navController.navigate(Routes.nowPlaying(trackId))
                },
            )
        }

        composable(
            route = "now_playing?trackId={trackId}",
            arguments = listOf(
                navArgument("trackId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId")
            NowPlayingScreen(
                trackId = trackId,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.DOWNLOADS) {
            PlaceholderScreen("Downloads")
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center,
        )
    }
}
