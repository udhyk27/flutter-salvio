package com.ydh.salvio

import android.os.Bundle
import androidx.activity.ComponentActivity
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
        // 시스템 다크/라이트 설정에 따라 시스템 바 아이콘 명암이 자동 반전됨
        enableEdgeToEdge()
        setContent {
            SalvioTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SalvioNavGraph()
                }
            }
        }
    }
}
