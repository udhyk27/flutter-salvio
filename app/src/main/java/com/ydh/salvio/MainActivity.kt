package com.ydh.salvio

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ydh.salvio.ui.navigation.SalvioNavGraph
import com.ydh.salvio.ui.theme.SalvioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 앱은 항상 다크 테마이므로 시스템 바 아이콘을 밝게 고정
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            SalvioTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SalvioNavGraph()
                }
            }
        }
    }
}
