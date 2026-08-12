package com.example.idlegame

fun greet(name: String): String =
    if (name.isEmpty()) "Hello, World!" else "Hello, ${name}!"

// live gate probe 2026-08-12: intentional compile error (reverted right after)
val __gate_probe: Int = "not an int"
