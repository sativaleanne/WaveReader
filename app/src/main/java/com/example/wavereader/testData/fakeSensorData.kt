package com.example.wavereader.testData

data class FakeSensorData(
    val timestamp: Long, // nanoseconds
    val ax: Float, val ay: Float, val az: Float,  // Acceleration
    val gx: Float, val gy: Float, val gz: Float   // Angular velocity
)

// Simulated floating motion with small oscillations
val fakeSensorData = listOf(
    FakeSensorData(0L, 0.02f, 0.03f, 9.81f, 0.001f, -0.002f, 0.003f),
    FakeSensorData(20_000_000L, 0.03f, 0.04f, 9.80f, 0.002f, -0.001f, 0.004f),
    FakeSensorData(40_000_000L, 0.01f, 0.02f, 9.79f, -0.001f, 0.000f, 0.003f),
    FakeSensorData(60_000_000L, -0.01f, 0.01f, 9.78f, -0.002f, 0.001f, 0.002f),
    FakeSensorData(80_000_000L, -0.02f, -0.01f, 9.80f, -0.003f, 0.002f, 0.001f),
    FakeSensorData(100_000_000L, -0.03f, -0.02f, 9.82f, -0.004f, 0.003f, 0.000f),
    FakeSensorData(120_000_000L, -0.01f, -0.03f, 9.83f, -0.003f, 0.002f, -0.001f),
    FakeSensorData(140_000_000L, 0.01f, -0.04f, 9.81f, -0.002f, 0.001f, -0.002f),
    FakeSensorData(160_000_000L, 0.03f, -0.03f, 9.79f, -0.001f, 0.000f, -0.003f),
    FakeSensorData(180_000_000L, 0.04f, -0.02f, 9.77f, 0.000f, -0.001f, -0.004f),
    FakeSensorData(200_000_000L, 0.03f, -0.01f, 9.76f, 0.001f, -0.002f, -0.003f),
    FakeSensorData(220_000_000L, 0.02f, 0.00f, 9.78f, 0.002f, -0.003f, -0.002f),
    FakeSensorData(240_000_000L, 0.01f, 0.02f, 9.80f, 0.003f, -0.004f, -0.001f),
    FakeSensorData(260_000_000L, -0.01f, 0.03f, 9.81f, 0.004f, -0.003f, 0.000f),
    FakeSensorData(280_000_000L, -0.03f, 0.04f, 9.79f, 0.003f, -0.002f, 0.001f),
    FakeSensorData(300_000_000L, -0.04f, 0.03f, 9.78f, 0.002f, -0.001f, 0.002f),
    FakeSensorData(320_000_000L, -0.03f, 0.02f, 9.76f, 0.001f, 0.000f, 0.003f),
    FakeSensorData(340_000_000L, -0.02f, 0.01f, 9.77f, 0.000f, 0.001f, 0.004f),
    FakeSensorData(360_000_000L, -0.01f, 0.00f, 9.79f, -0.001f, 0.002f, 0.003f),
    FakeSensorData(380_000_000L, 0.01f, -0.01f, 9.81f, -0.002f, 0.003f, 0.002f),
    FakeSensorData(400_000_000L, 0.02f, -0.02f, 9.83f, -0.003f, 0.004f, 0.001f),
    FakeSensorData(420_000_000L, 0.03f, -0.03f, 9.82f, -0.004f, 0.003f, 0.000f),
    FakeSensorData(440_000_000L, 0.04f, -0.04f, 9.80f, -0.003f, 0.002f, -0.001f),
    FakeSensorData(460_000_000L, 0.03f, -0.03f, 9.78f, -0.002f, 0.001f, -0.002f),
    FakeSensorData(480_000_000L, 0.02f, -0.02f, 9.76f, -0.001f, 0.000f, -0.003f)
)