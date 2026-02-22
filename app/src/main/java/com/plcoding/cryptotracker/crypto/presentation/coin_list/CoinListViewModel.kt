package com.plcoding.cryptotracker.crypto.presentation.coin_list

import android.R.attr.data
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.cryptotracker.core.domain.util.onError
import com.plcoding.cryptotracker.core.domain.util.onSuccess
import com.plcoding.cryptotracker.crypto.domain.Coin
import com.plcoding.cryptotracker.crypto.domain.CoinDataSource
import com.plcoding.cryptotracker.crypto.presentation.models.toCoinUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoinListViewModel(
    private val coinDataSource: CoinDataSource
): ViewModel() {

    val _state = MutableStateFlow(CoinListState()) // hot
    val state = _state
        .onStart { // cold operation
            loadCoin()
        }
        .stateIn( // converts to hot
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CoinListState()
        )
    private val _events = Channel<CoinListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CoinListAction) {
        when (action) {
            is CoinListAction.onCoinClick -> {

            }
        }
    }

    private fun loadCoin() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )}
            coinDataSource
                .getCoins()
                .onSuccess { coins: List<Coin> ->
                    _state.update { it ->
                        it.copy(
                            isLoading = false,
                            coins = coins.map { coin ->
                                coin.toCoinUi()
                            }
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading  = false
                        )
                    }

                    _events.send(CoinListEvent.Error(error))
                }
        }
    }
}
