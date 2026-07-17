package com.ydh.salvio

import android.app.Application
import com.ydh.salvio.data.api.RetrofitClient
import com.ydh.salvio.data.local.AppDatabase
import com.ydh.salvio.data.local.TokenDataStore
import com.ydh.salvio.data.repository.GitHubRepository

class SalvioApplication : Application() {
    lateinit var tokenDataStore: TokenDataStore
    lateinit var database: AppDatabase

    private var cachedRepository: Pair<String, GitHubRepository>? = null

    /**
     * 토큰에 대응하는 GitHubRepository를 반환한다.
     * 동일 토큰이면 캐시된 인스턴스를 재사용하므로, 각 ViewModel이 호출마다
     * Repository를 새로 생성하던 중복을 없앤다.
     */
    @Synchronized
    fun githubRepository(token: String): GitHubRepository {
        cachedRepository?.let { (t, repo) -> if (t == token) return repo }
        return GitHubRepository(RetrofitClient.create(token), database.cacheDao())
            .also { cachedRepository = token to it }
    }

    override fun onCreate() {
        super.onCreate()
        tokenDataStore = TokenDataStore(this)
        database = AppDatabase.getInstance(this)
    }
}
