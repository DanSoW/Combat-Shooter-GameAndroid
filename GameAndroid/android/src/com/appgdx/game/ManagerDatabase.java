package com.appgdx.game;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Nullable;

public class ManagerDatabase extends SQLiteOpenHelper {

    //Название базы данных
    public static final String DATABASE_NAME = "DataBaseSQL";

    //Название таблиц
    public static final String REGISTER = "Register";
    public static final String RATING = "RatingScore";

    //Таблица зарегистрированных пользователей
    public static final String LOGIN = "login";
    public static final String PASSWORD = "pass";

    //Таблица содержащая рейтинг каждого пользователя
    public static final String ID = "id";
    public static final String FK_LOGIN = "login_fk";
    public static final String SCORE = "score";

    private static final int DATABASE_VERSION = 3;

    public ManagerDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion == 1){
            try{
                db.execSQL("CREATE TABLE " + REGISTER + " ("
                        + LOGIN + " TEXT PRIMARY KEY, " +
                        PASSWORD + " TEXT);"
                );
            }catch(Exception e){}
        }else if(newVersion == 3){
            try{
                db.execSQL("CREATE TABLE " + RATING +
                        " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + FK_LOGIN + " TEXT, " + SCORE + " INTEGER, " +
                        "FOREIGN KEY (" + FK_LOGIN + ") REFERENCES " + REGISTER + "(" + LOGIN + "));"
                );
            }catch(Exception e){}
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        updateDatabase(db, oldVersion, newVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, DATABASE_VERSION);
    }
}
