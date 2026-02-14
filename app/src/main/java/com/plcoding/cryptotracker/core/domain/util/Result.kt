package com.plcoding.cryptotracker.core.domain.util

typealias DomainError = Error

sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: DomainError>(val error: E): Result<Nothing, E>
}

inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when(this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map {  }
}

inline fun <T, E: Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}
inline fun <T, E: Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

typealias EmptyResult<E> = Result<Unit, E>


/*
sealed interface NetworkError : Error
data class TimeoutError(val message: String) : NetworkError
data class ServerError(val code: Int) : NetworkError

// And use them flexibly
val specificError: Result<String, TimeoutError> = Result.Error(TimeoutError("Slow connection"))
val generalError: Result<String, NetworkError> = specificError  // ✅ Works because of 'out'

val result: Result<Int, DomainError> = Result.Success(5)

// or even more explicit:
val doubled = result.map(fun (number: Int): Int {
    return number * 2
})

val res: Result<Int, DomainError> = result
.onSuccess { data -> println("Got: $data") }
    .onError { error -> println("Failed: $error") }

*/
