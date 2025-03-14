package com.example.calendar_app.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;

import com.example.calendar_app.Entities.UserEntity;

import java.util.UUID;

@Dao
public interface UserDAO {

   @Insert
   void insert(UserEntity user);
   @Update
   void update(UserEntity user);
}
