package dev.tidesapp.wearos.library.ui.albums

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import coil3.compose.AsyncImage
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.tidesapp.wearos.core.domain.model.AlbumItem
import dev.tidesapp.wearos.core.ui.components.ErrorScreen
import dev.tidesapp.wearos.core.ui.components.LoadingScreen
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AlbumsScreen(
    onNavigateToAlbumDetail: (String) -> Unit,
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is AlbumsUiEffect.NavigateToAlbumDetail -> onNavigateToAlbumDetail(effect.albumId)
                is AlbumsUiEffect.ShowError -> { /* handled by snackbar in real app */ }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (uiState is AlbumsUiState.Initial) {
            viewModel.onEvent(AlbumsUiEvent.LoadAlbums)
        }
    }
}

@Composable
fun AlbumsContent(
    uiState: AlbumsUiState,
    onEvent: (AlbumsUiEvent) -> Unit,
) {
    when (uiState) {
        AlbumsUiState.Initial,
        AlbumsUiState.Loading -> LoadingScreen()

        is AlbumsUiState.Success -> AlbumsList(
            albums = uiState.albums,
            onEvent = onEvent,
        )

        is AlbumsUiState.Refreshing -> AlbumsList(
            albums = uiState.albums,
            onEvent = onEvent,
        )

        is AlbumsUiState.Error -> ErrorScreen(
            message = uiState.message,
            onRetry = { onEvent(AlbumsUiEvent.RetryLoad) },
        )
    }
}

@Composable
private fun AlbumsList(
    albums: ImmutableList<AlbumItem>,
    onEvent: (AlbumsUiEvent) -> Unit,
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip,
        ),
    )

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        item {
            Text(
                text = "Albums",
                style = MaterialTheme.typography.title2,
                color = MaterialTheme.colors.primary,
            )
        }
        items(albums.size) { index ->
            val album = albums[index]
            AlbumChip(
                album = album,
                onClick = { onEvent(AlbumsUiEvent.AlbumClicked(album)) },
            )
        }
    }
}

@Composable
private fun AlbumChip(
    album: AlbumItem,
    onClick: () -> Unit,
) {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        label = {
            Text(
                text = album.title,
                style = MaterialTheme.typography.button,
                maxLines = 1,
            )
        },
        secondaryLabel = {
            Text(
                text = album.artistName,
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurfaceVariant,
                maxLines = 1,
            )
        },
        icon = {
            AsyncImage(
                model = album.imageUrl,
                contentDescription = album.title,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
            )
        },
        colors = ChipDefaults.secondaryChipColors(),
    )
}
