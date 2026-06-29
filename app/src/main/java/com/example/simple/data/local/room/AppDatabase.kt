package com.example.simple.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.simple.data.local.room.dao.ItemDao
import com.example.simple.data.local.room.entity.ItemEntity

@Database(entities = [ItemEntity::class], version = 1)
abstract class  AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
