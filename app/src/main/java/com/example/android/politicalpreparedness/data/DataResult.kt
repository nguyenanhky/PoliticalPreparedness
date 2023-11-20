package com.example.android.politicalpreparedness.data.dto
/**
 * A sealed class that encapsulates successful outcome with a value of type [T]
 * or a failure with message and statusCode
 */
sealed class DataResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : DataResult<T>()
    data class Error(val message: String?, val statusCode: Int? = null) :
        DataResult<Nothing>()
}

/**
 * Checks whether [Result] is of type [Success] and its [Success.data] is not null.
 */
val DataResult<*>.succeeded: Boolean
    get() = when (this) {
        is DataResult.Success -> data != null
        else -> false
    }
