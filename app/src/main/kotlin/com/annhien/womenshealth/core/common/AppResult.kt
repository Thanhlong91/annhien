package com.annhien.womenshealth.core.common

/**
 * A generic wrapper for operation results throughout the app.
 * Prevents exceptions from propagating through the ViewModel → UI boundary.
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val errorType: ErrorType = ErrorType.UNKNOWN
    ) : AppResult<Nothing>()
    object Loading : AppResult<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw cause ?: RuntimeException(message)
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Error) -> Unit): AppResult<T> {
        if (this is Error) action(this)
        return this
    }

    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }
}

enum class ErrorType {
    DATABASE,
    FILE_IO,
    SECURITY,
    VALIDATION,
    INSUFFICIENT_STORAGE,
    CORRUPT_DATA,
    VERSION_MISMATCH,
    WRONG_PASSWORD,
    UNKNOWN
}

/**
 * Extension to wrap a suspend block in AppResult, catching all exceptions.
 */
suspend inline fun <T> safeCall(crossinline block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: Exception) {
        AppResult.Error(
            message = e.message ?: "Đã xảy ra lỗi không xác định",
            cause = e
        )
    }
}
