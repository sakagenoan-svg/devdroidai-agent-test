package com.example.idlegame

fun formatCoin(value: Long): String {
    return when {
        value < 1_000 -> value.toString()
        value < 1_000_000 -> {
            val k = value / 1_000.0
            String.format("%.1fK", k).replace(",", "")
        }
        value < 1_000_000_000 -> {
            val m = value / 1_000_000.0
            String.format("%.1fM", m).replace(",", "")
        }
        else -> {
            val b = value / 1_000_000_000.0
            String.format("%.1fB", b).replace(",", "")
        }
    }
}
