package com.ll.hotel.standard.util

import com.ll.hotel.global.app.AppConfig
import com.ll.hotel.global.exceptions.CustomS3Exception
import com.ll.hotel.global.exceptions.ServiceException
import com.ll.hotel.global.response.RsData
import org.aspectj.lang.JoinPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import java.util.*

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

object LogUtil {
    fun logControllerRequest(log: Logger, className: String, methodName: String) {
        log.info("Request Controller = [{}.{}]", className, methodName)
    }

    fun logControllerResponse(log: Logger, className: String, methodName: String, rsData: RsData<*>) {
        val jsonData = AppConfig.objectMapper.writeValueAsString(rsData.data)

        log.info(
            "Response Controller = [{}.{}], status: [{}], message: [{}], data: [{}]",
            className, methodName, rsData.resultCode, rsData.msg, jsonData
        )
    }

    fun logServiceRequest(log: Logger, className: String, methodName: String) {
        checkValidLog(className, methodName).takeIf { it }
            ?.let { log.info("Request Service = [{}.{}]", className, methodName) }
    }

    fun logError(log: Logger, joinPoint: JoinPoint, ex: Throwable) {
        val className = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name
        val status: String
        val msg: String

        when (ex) {
            is ServiceException -> {
                status = ex.resultCode.toString()
                msg = ex.msg
            }

            is CustomS3Exception -> {
                status = ex.resultCode.toString()
                msg = ex.msg
            }

            is MethodArgumentNotValidException -> {
                status = HttpStatus.BAD_REQUEST.toString()
                msg = ex.message
            }

            else -> {
                status = "UNKNOWN"
                msg = ex.message ?: "Error occurred"
            }
        }

        log.error(
            "ERROR = [{}.{}], status: [{}], message: [{}]",
            className, methodName, status, msg
        )
    }

    private fun checkValidLog(className: String, methodName: String): Boolean {
        return when (className) {
            "MemberService", "CustomOAuth2UserService" -> methodName == "join"
                    || methodName.lowercase().run {
                contains("login") || contains("logout")
            }

            "AuthTokenService", "RefreshTokenService" -> false
            else -> true
        }
    }
}