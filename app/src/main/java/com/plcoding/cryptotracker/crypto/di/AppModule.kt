package com.plcoding.cryptotracker.crypto.di

import com.plcoding.cryptotracker.core.data.networking.HttpClientFactory
import com.plcoding.cryptotracker.crypto.data.networking.RemoteCoinDataSource
import com.plcoding.cryptotracker.crypto.domain.CoinDataSource
import com.plcoding.cryptotracker.crypto.presentation.coin_list.CoinListViewModel
import io.ktor.client.engine.cio.CIO
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val appModule = module {
    single {
        HttpClientFactory.create(engine = CIO.create())
    }

    /*single {
        RemoteCoinDataSource(get()) // get() will find relevant dependency in it's module and inject it
    }*/

    singleOf(::RemoteCoinDataSource).bind<CoinDataSource>() // shortcut

    viewModelOf(::CoinListViewModel)
}
