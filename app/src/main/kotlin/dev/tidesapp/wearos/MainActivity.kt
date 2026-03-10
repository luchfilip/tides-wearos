package dev.tidesapp.wearos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.tidesapp.wearos.core.ui.theme.TidesTheme
import dev.tidesapp.wearos.nav.TidesNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TidesTheme {
                TidesNavGraph()
            }
        }
    }
}
