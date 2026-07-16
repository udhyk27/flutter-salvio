package com.ydh.salvio.data.api

import com.ydh.salvio.data.model.AccessTokenResponse
import com.ydh.salvio.data.model.DeviceCodeResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * GitHub OAuth Device Flow 엔드포인트.
 * base URL 은 api.github.com 이 아닌 https://github.com/ 이다.
 */
interface GitHubOAuthApi {

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("login/device/code")
    suspend fun requestDeviceCode(
        @Field("client_id") clientId: String,
        @Field("scope") scope: String
    ): DeviceCodeResponse

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("login/oauth/access_token")
    suspend fun requestAccessToken(
        @Field("client_id") clientId: String,
        @Field("device_code") deviceCode: String,
        @Field("grant_type") grantType: String = "urn:ietf:params:oauth:grant-type:device_code"
    ): AccessTokenResponse
}
