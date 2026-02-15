package com.plcoding.cryptotracker.crypto.domain

import com.plcoding.cryptotracker.core.domain.util.NetworkError
import com.plcoding.cryptotracker.core.domain.util.Result


// domain layer is all about what not how
// so just mention what does each part of function mean.

interface CoinDataSource {
    suspend fun getCoins(): Result<List<Coin>, NetworkError>
}
