package com.example.simple.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.simple.data.local.database.dao.ItemDao
import com.example.simple.data.local.database.dao.OrganizationDao
import com.example.simple.data.local.database.dao.TransactionDao
import com.example.simple.data.local.database.dao.UserDao
import com.example.simple.data.local.database.entity.ItemEntity
import com.example.simple.data.local.database.entity.OrganizationEntity
import com.example.simple.data.local.database.entity.TransactionEntity
import com.example.simple.data.local.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        OrganizationEntity::class,
        ItemEntity::class,
        TransactionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun itemDao(): ItemDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "simple_app.db"
    }
}