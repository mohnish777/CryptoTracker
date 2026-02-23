package com.plcoding.cryptotracker

import android.R.attr.top
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.plcoding.cryptotracker.core.presentation.util.observeAsEvents
import com.plcoding.cryptotracker.core.presentation.util.toString
import com.plcoding.cryptotracker.crypto.presentation.coin_detail.CoinDetailScreen
import com.plcoding.cryptotracker.crypto.presentation.coin_list.CoinListEvent
import com.plcoding.cryptotracker.crypto.presentation.coin_list.CoinListScreen
import com.plcoding.cryptotracker.crypto.presentation.coin_list.CoinListState
import com.plcoding.cryptotracker.crypto.presentation.coin_list.CoinListViewModel
import com.plcoding.cryptotracker.ui.theme.CryptoTrackerTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = koinViewModel<CoinListViewModel>()
                    val state by viewModel.state.collectAsState()
                    val context = LocalContext.current
                    observeAsEvents(events = viewModel.events) { event ->
                        when (event) {
                            is CoinListEvent.Error -> {
                                Toast
                                    .makeText(
                                        context,
                                        event.error.toString(context),
                                        Toast.LENGTH_LONG)
                                    .show()
                            }
                        }

                    }

                    when {
                        state.selectedCoin !=null -> {
                            CoinDetailScreen(
                                state = state,
                                modifier = Modifier
                                    .padding(innerPadding)
                            )
                        }

                        else -> {
                            CoinListScreen(
                                state = state,
                                onAction = viewModel::onAction,
                                modifier = Modifier.padding(
                                    top = innerPadding.calculateTopPadding(),
                                    bottom = innerPadding.calculateBottomPadding()
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
