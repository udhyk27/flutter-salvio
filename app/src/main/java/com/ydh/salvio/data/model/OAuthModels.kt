package com.ydh.salvio.data.model

import com.google.gson.annotations.SerializedName

/** POST https://github.com/login/device/code 응답 */
data class DeviceCodeResponse(
    @SerializedName("device_code") val deviceCode: String,
    @SerializedName("user_code") val userCode: String,
    @SerializedName("verification_uri") val verificationUri: String,
    @SerializedName("expires_in") val expiresIn: Int,
    val interval: Int
)

/** POST https://github.com/login/oauth/access_token 응답 (Accept: application/json) */
data class AccessTokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    val scope: String?,
    // 폴링 중 미인증 시: authorization_pending / slow_down / expired_token / access_denied
    val error: String?,
    @SerializedName("error_description") val errorDescription: String?
)
