package com.deliveryapp.customer.core.network

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(call())
} catch (exception: CancellationException) {
    throw exception
} catch (exception: HttpException) {
    val status = exception.code()
    val apiError = runCatching {
        exception.response()?.errorBody()?.string()?.let {
            Gson().fromJson(it, ApiErrorResponse::class.java)
        }
    }.getOrNull()
    ApiResult.Error(
        code = apiError?.code,
        message = apiError?.message ?: defaultHttpMessage(status),
        httpStatus = status
    )
} catch (_: IOException) {
    ApiResult.Error(
        code = "NETWORK_ERROR",
        message = "Không thể kết nối máy chủ. Hãy kiểm tra Backend và kết nối mạng."
    )
} catch (exception: Exception) {
    ApiResult.Error(
        code = "UNEXPECTED_ERROR",
        message = exception.message ?: "Đã xảy ra lỗi không xác định."
    )
}

private fun defaultHttpMessage(status: Int): String = when (status) {
    400 -> "Dữ liệu gửi lên không hợp lệ."
    401 -> "Phiên đăng nhập đã hết hạn."
    403 -> "Bạn không có quyền thực hiện thao tác này."
    404 -> "Không tìm thấy dữ liệu yêu cầu."
    409 -> "Dữ liệu đã thay đổi. Vui lòng tải lại và thử lại."
    else -> "Máy chủ trả về lỗi $status."
}
