package com.kapdroid.pomtom.common

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    fun getOrNull(): T? = (this as? Success)?.value
    fun errorOrNull(): AppError? = (this as? Failure)?.error
}

inline fun <T> runCatchingDomain(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (cancellation: kotlin.coroutines.cancellation.CancellationException) {
    throw cancellation
} catch (t: Throwable) {
    Result.Failure(AppError.Unknown(t.message ?: t::class.simpleName ?: "unknown", t))
}

sealed class AppError(open val message: String) {
    data class NotFound(override val message: String) : AppError(message)
    data class Validation(override val message: String) : AppError(message)
    data class IO(override val message: String, val cause: Throwable? = null) : AppError(message)
    data class Unsupported(override val message: String) : AppError(message)
    data class Cancelled(override val message: String = "cancelled") : AppError(message)
    data class Unknown(override val message: String, val cause: Throwable? = null) : AppError(message)
}
