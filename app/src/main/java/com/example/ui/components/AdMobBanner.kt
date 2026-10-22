package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.game.ads.AdConfig
import com.example.game.ads.AdMobManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.getBannerId()
) {
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(1.dp, Color(0xFFFFD700).copy(alpha = if (isAdLoaded) 0.4f else 0.15f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                try {
                    AdView(ctx).apply {
                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isAdLoaded = true
                            }
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                isAdLoaded = false
                            }
                        }
                        try {
                            val adRequest = AdRequest.Builder().build()
                            loadAd(adRequest)
                        } catch (e: Throwable) {
                            isAdLoaded = false
                        }
                    }
                } catch (e: Throwable) {
                    isAdLoaded = false
                    android.view.View(ctx)
                }
            },
            onRelease = { view ->
                try {
                    if (view is AdView) {
                        view.destroy()
                    }
                } catch (_: Throwable) {}
            }
        )
    }
}
