package com.example.hesabak;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Hesabak.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // جدول تنظیمات و رمز ورود
        db.execSQL("CREATE TABLE settings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pin_hash TEXT NOT NULL, " +
                "created_at TEXT NOT NULL)");

        // جدول تراکنش‌ها
        db.execSQL("CREATE TABLE transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT NOT NULL CHECK(type IN ('income','expense')), " +
                "amount INTEGER NOT NULL CHECK(amount > 0), " +
                "title TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "notes TEXT, " +
                "created_at TEXT NOT NULL)");

        // جدول بودجه‌ها
        db.execSQL("CREATE TABLE budgets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT NOT NULL, " +
                "month TEXT NOT NULL, " +
                "amount INTEGER NOT NULL CHECK(amount >= 0), " +
                "UNIQUE(category, month))");

        // برای سریع‌تر شدن فیلترهای تراکنش‌ها
        db.execSQL("CREATE INDEX idx_transactions_date " +
                "ON transactions(date)");

        db.execSQL("CREATE INDEX idx_transactions_category_date " +
                "ON transactions(category, date)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // تغییرات نسخه‌های بعدی دیتابیس را اینجا می‌نویسیم.
    }
}
