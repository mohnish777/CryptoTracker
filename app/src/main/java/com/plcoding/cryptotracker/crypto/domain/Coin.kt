package com.plcoding.cryptotracker.crypto.domain

data class Coin(
    val id: String,
    val rank: Int,
    val name: String,
    val marketCapUsd: Double,
    val symbol: String,
    val priceUsd: Double,
    val changePercent24Hr: Double
)
