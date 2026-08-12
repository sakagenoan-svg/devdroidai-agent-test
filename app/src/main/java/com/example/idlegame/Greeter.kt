package com.example.idlegame

fun greet(name: String): String =
    if (name.isEmpty()) "Hello, World!" else "Hello, ${name}!"
