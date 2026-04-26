package com.kapdroid.pomtom.database

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(factory: DatabaseFactory): PomtomDatabase =
    PomtomDatabase(factory.createDriver())
