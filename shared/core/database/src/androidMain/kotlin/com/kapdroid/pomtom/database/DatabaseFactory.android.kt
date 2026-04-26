package com.kapdroid.pomtom.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = PomtomDatabase.Schema,
        context = context.applicationContext,
        name = DB_FILE_NAME,
    )

    companion object {
        const val DB_FILE_NAME = "pomtom.db"
    }
}
