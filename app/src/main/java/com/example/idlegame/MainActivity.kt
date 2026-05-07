package com.example.idlegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import com.example.idlegame.ui.theme.IdleGameTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BgColor = Color(0xFF0D1117)
val SurfaceColor = Color(0xFF161B22)
val AccentColor = Color(0xFF58A6FF)
val GoldColor = Color(0xFFD4A017)
val GreenColor = Color(0xFF3FB950)
val TextPrimary = Color(0xFFE6EDF3)
val TextSecondary = Color(0xFF8B949E)

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IdleGameTheme {
                IdleGameScreen(viewModel)
            }
        }
    }
}

@Composable
fun IdleGameScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // ヘッダー
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("🪙 コインクリッカー", color = AccentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${formatNumber(state.coins)} コイン",
                    color = GoldColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "+${formatNumber(state.coinsPerSecond)}/秒",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        // メインクリックボタン
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(AccentColor.copy(alpha = 0.15f))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = AccentColor)
                    ) { vm.onClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🪙", fontSize = 48.sp)
                    Text(
                        "+${formatNumber(state.coinsPerClick)}",
                        color = AccentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // アップグレード一覧
        Text(
            "アップグレード",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.upgrades) { upgrade ->
                UpgradeCard(
                    upgrade = upgrade,
                    canAfford = state.coins >= upgrade.cost,
                    onBuy = { vm.buyUpgrade(upgrade.id) }
                )
            }
        }
    }
}

@Composable
fun UpgradeCard(upgrade: Upgrade, canAfford: Boolean, onBuy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(upgrade.emoji, fontSize = 32.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(upgrade.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(upgrade.description, color = TextSecondary, fontSize = 12.sp)
            Text("所持: ${upgrade.count}個", color = AccentColor, fontSize = 12.sp)
        }
        Button(
            onClick = onBuy,
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenColor,
                disabledContainerColor = SurfaceColor.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.wrapContentSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("購入", fontSize = 12.sp, color = TextPrimary)
                Text("🪙${formatNumber(upgrade.cost)}", fontSize = 11.sp, color = TextPrimary)
            }
        }
    }
}

fun formatNumber(n: Long): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000.0)
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fK".format(n / 1_000.0)
    else -> n.toString()
}