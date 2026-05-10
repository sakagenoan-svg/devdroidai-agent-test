package com.example.idlegame

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlin.math.pow

data class Upgrade(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val baseCost: Long,
    val baseCoinsPerSec: Long,
    val baseCoinsPerClick: Long,
    val count: Int = 0
) {
    val cost: Long get() = (baseCost * 1.15.pow(count.toDouble())).toLong()
}

data class GameState(
    val coins: Long = 0,
    val coinsPerClick: Long = 1,
    val coinsPerSecond: Long = 0,
    val upgrades: List<Upgrade> = initialUpgrades()
)

fun initialUpgrades() = listOf(
    Upgrade(
        id = "cursor",
        name = "カーソル",
        emoji = "👆",
        description = "クリックで+1コイン増加",
        baseCost = 10,
        baseCoinsPerSec = 0,
        baseCoinsPerClick = 1
    ),
    Upgrade(
        id = "farm",
        name = "コイン農場",
        emoji = "🌾",
        description = "毎秒+1コイン",
        baseCost = 50,
        baseCoinsPerSec = 1,
        baseCoinsPerClick = 0
    ),
    Upgrade(
        id = "mine",
        name = "コイン鉱山",
        emoji = "⛏️",
        description = "毎秒+5コイン",
        baseCost = 200,
        baseCoinsPerSec = 5,
        baseCoinsPerClick = 0
    ),
    Upgrade(
        id = "factory",
        name = "コイン工場",
        emoji = "🏭",
        description = "毎秒+20コイン",
        baseCost = 1_000,
        baseCoinsPerSec = 20,
        baseCoinsPerClick = 0
    ),
    Upgrade(
        id = "bank",
        name = "コイン銀行",
        emoji = "🏦",
        description = "毎秒+100コイン",
        baseCost = 5_000,
        baseCoinsPerSec = 100,
        baseCoinsPerClick = 0
    ),
    Upgrade(
        id = "lab",
        name = "コイン研究所",
        emoji = "🔬",
        description = "毎秒+500コイン",
        baseCost = 25_000,
        baseCoinsPerSec = 500,
        baseCoinsPerClick = 0
    ),
    Upgrade(
        id = "rocket",
        name = "コインロケット",
        emoji = "🚀",
        description = "毎秒+2000コイン",
        baseCost = 100_000,
        baseCoinsPerSec = 2_000,
        baseCoinsPerClick = 0
    )
)

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var coinGenerationReceiver: BroadcastReceiver? = null
    private var context: Context? = null

    fun initialize(appContext: Context) {
        this.context = appContext
        startCoinGeneration()
    }

    private fun startCoinGeneration() {
        // 毎秒コイン自動生成（UIスレッドで実行）
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _state.value = _state.value.copy(
                    coins = _state.value.coins + _state.value.coinsPerSecond
                )
            }
        }

        // Foreground Service からのブロードキャストを受信
        context?.let { appContext ->
            coinGenerationReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    // Service からのコイン生成通知
                    if (intent?.action == ACTION_COIN_GENERATION) {
                        // ViewModel のコルーチンで処理されているので、ここでは追加の処理は不要
                        // Service が実際に生成している場合の同期用
                    }
                }
            }

            val filter = IntentFilter(ACTION_COIN_GENERATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(coinGenerationReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                appContext.registerReceiver(coinGenerationReceiver, filter)
            }
        }
    }

    fun onClick() {
        _state.value = _state.value.copy(
            coins = _state.value.coins + _state.value.coinsPerClick
        )
    }

    fun buyUpgrade(upgradeId: String) {
        val current = _state.value
        val upgrade = current.upgrades.find { it.id == upgradeId } ?: return
        if (current.coins < upgrade.cost) return

        val newUpgrades = current.upgrades.map {
            if (it.id == upgradeId) it.copy(count = it.count + 1) else it
        }

        val newCoinsPerSec = newUpgrades.sumOf { it.baseCoinsPerSec * it.count }
        val newCoinsPerClick = newUpgrades.sumOf { it.baseCoinsPerClick * it.count }.coerceAtLeast(1)

        _state.value = current.copy(
            coins = current.coins - upgrade.cost,
            upgrades = newUpgrades,
            coinsPerSecond = newCoinsPerSec,
            coinsPerClick = newCoinsPerClick
        )
    }

    fun startBackgroundGeneration() {
        context?.let {
            val intent = Intent(it, CoinGenerationService::class.java)
            intent.action = CoinGenerationService.ACTION_START
            it.startService(intent)
        }
    }

    fun stopBackgroundGeneration() {
        context?.let {
            val intent = Intent(it, CoinGenerationService::class.java)
            intent.action = CoinGenerationService.ACTION_STOP
            it.startService(intent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        coinGenerationReceiver?.let { receiver ->
            context?.unregisterReceiver(receiver)
        }
        stopBackgroundGeneration()
    }
}
