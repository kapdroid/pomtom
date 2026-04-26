package com.kapdroid.pomtom.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = PomtomDatabase.Schema,
        name = DB_FILE_NAME,
    )

    companion object {
        const val DB_FILE_NAME = "pomtom.db"
    }
}
