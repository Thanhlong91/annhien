package com.annhien.womenshealth.core.connectivity

sealed class OnlineResult<out T> {
    data class Success<T>(val data: T, val fromCache: Boolean = false) : OnlineResult<T>()

    sealed class Error : OnlineResult<Nothing>() {
        object Offline : Error()
        object RateLimited : Error()
        object InvalidApiKey : Error()
        object NotFound : Error()
        object ServiceUnavailable : Error()
        data class Unknown(val message: String?) : Error()
    }
}
