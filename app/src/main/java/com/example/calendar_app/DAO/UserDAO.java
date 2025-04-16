package com.example.calendar_app.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.calendar_app.Entities.UserEntity;

import java.util.UUID;

@Dao
public interface UserDAO {

   @Insert
   void insert(UserEntity user);

   @Query("SELECT * FROM users WHERE id = :id")
   UserEntity getUserById(String id);

   @Update
   void update(UserEntity user);
   
   @Query("SELECT * FROM users WHERE phone = :phone AND password = :password")
   UserEntity getUserByPhoneAndPassword(String phone, String password);

   @Query("SELECT * FROM users WHERE phone = :phone")
   UserEntity getUserByPhone(String phone);
}
