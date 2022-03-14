package com.app.bible.knowbible.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.app.bible.knowbible.mvvm.model.HighlightedBibleTextInfoModel
import com.app.bible.knowbible.utility.Utility
import io.reactivex.Single
import java.util.*

class HighlightedBibleTextInfoDBHelper private constructor() {
    //Используется Singleton, чтобы правильно распределить ресурсы памяти на подключение БД. Подключение к БД достаточно затратно по времени,
    //поэтому если подключения потенциально могут быть частыми, то лучше держать подключение постоянно активным. Singleton позволяет это осуществить.
    companion object {
        private lateinit var dbHelp: DBHelp
        private lateinit var dataBase: SQLiteDatabase
        private lateinit var cv: ContentValues

        private var instance: HighlightedBibleTextInfoDBHelper? = null
        fun getInstance(context: Context?): HighlightedBibleTextInfoDBHelper? {
            if (instance == null) {
                instance = HighlightedBibleTextInfoDBHelper()
                dbHelp = context?.let { instance!!.DBHelp(it) }!!
                dataBase = dbHelp.writableDatabase
                cv = ContentValues()
            }
            return instance
        }

        const val BIBLE_TEXT_INFO_BASE_NAME = "myBibleTextInfoDB"
    }

//    companion object {
//        const val BIBLE_TEXT_INFO_BASE_NAME = "myBibleTextInfoDB"
//    }

//    private var dbHelp: DBHelp
//    private var dataBase: SQLiteDatabase
//    private var cv: ContentValues
//
//    init {
//        dbHelp = DBHelp(context)
//        dataBase = dbHelp.writableDatabase
//        cv = ContentValues()
//    }

    //    fun loadBibleTextInfo(bookNumber: Int, chapterNumber: Int): Single<ArrayList<BibleTextInfoModel>> {
    fun loadBibleTextInfo(myBookNumber: Int, translation_name: String): Single<ArrayList<HighlightedBibleTextInfoModel>> {
        val cursor: Cursor =
                if (myBookNumber == -1) //Если на вход параметр -1, то собираем данные по всей Библии, если номер книги указан, то собираем данные только по конкретной книге
                    dataBase.query("my_bible_text_info_table",
                            null,
                            "translation_name == ?", //Фильтруем БД для поиска нужных текстов по конкретному обозначению перевода Библии
                            arrayOf(translation_name),
                            null,
                            null,
                            null)
                else
                    dataBase.query("my_bible_text_info_table",
                            null,
//                "book_number == ? AND chapter_number = ?", //Фильтруем БД для поиска нужных текстов в конкретной главе
//                arrayOf(bookNumber.toString(), chapterNumber.toString()),
                            "book_number == ? AND translation_name == ?", //Фильтруем БД для поиска нужных текстов в конкретной книге и по конкретному обозначению перевода Библии
                            arrayOf(myBookNumber.toString(), translation_name),
                            null,
                            null,
                            null)

        val collection = ArrayList<HighlightedBibleTextInfoModel>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex("id"))
                val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                val translationName = cursor.getString(cursor.getColumnIndex("translation_name"))
                val chapterNumber = cursor.getInt(cursor.getColumnIndex("chapter_number"))
                val verseNumber = cursor.getInt(cursor.getColumnIndex("verse_number"))
                val textColorHex = cursor.getString(cursor.getColumnIndex("text_color_hex"))
                val isTextBold = cursor.getInt(cursor.getColumnIndex("text_bold")) != 0 //Конвертируем int на boolean
                val isTextUnderline = cursor.getInt(cursor.getColumnIndex("text_underline")) != 0 //Конвертируем int на boolean

                collection.add(HighlightedBibleTextInfoModel(id, bookNumber, translationName, chapterNumber, verseNumber, textColorHex, isTextBold, isTextUnderline))

            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<HighlightedBibleTextInfoModel>> { collection }
    }

    fun loadDailyVersesInfo(): Single<ArrayList<HighlightedBibleTextInfoModel>> {
        val cursor = dataBase.query("my_bible_text_info_table",
                null,
                null,
                null,
                null,
                null,
                null)

        val collection = ArrayList<HighlightedBibleTextInfoModel>()

        if (cursor.moveToFirst()) {
            do {
                val isTextToDailyVerse = cursor.getInt(cursor.getColumnIndex("text_to_daily_verse")) != 0 //Конвертируем int на boolean
                //Добавляем в коллекцию только те тексты, которые были добавлены в "Стих дня"
                if (isTextToDailyVerse) {
                    val id = cursor.getLong(cursor.getColumnIndex("id"))
                    val bookNumber = cursor.getInt(cursor.getColumnIndex("book_number"))
                    val translationName = cursor.getString(cursor.getColumnIndex("translation_name"))
                    val chapterNumber = cursor.getInt(cursor.getColumnIndex("chapter_number"))
                    val verseNumber = cursor.getInt(cursor.getColumnIndex("verse_number"))
                    val textColorHex = cursor.getString(cursor.getColumnIndex("text_color_hex"))
                    val isTextBold = cursor.getInt(cursor.getColumnIndex("text_bold")) != 0 //Конвертируем int на boolean
                    val isTextUnderline = cursor.getInt(cursor.getColumnIndex("text_underline")) != 0 //Конвертируем int на boolean

                    collection.add(HighlightedBibleTextInfoModel(id, bookNumber, translationName, chapterNumber, verseNumber, textColorHex, isTextBold, isTextUnderline))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return Single.fromCallable<ArrayList<HighlightedBibleTextInfoModel>> { collection }
    }

    fun addBibleTextInfo(highlightedBibleTextInfo: HighlightedBibleTextInfoModel): Long {
        cv.put("book_number", highlightedBibleTextInfo.bookNumber)
        cv.put("translation_name", highlightedBibleTextInfo.translationName)
        cv.put("chapter_number", highlightedBibleTextInfo.chapterNumber)
        cv.put("verse_number", highlightedBibleTextInfo.verseNumber)
        cv.put("text_color_hex", highlightedBibleTextInfo.textColorHex)
        cv.put("text_bold", highlightedBibleTextInfo.isTextBold)
        cv.put("text_underline", highlightedBibleTextInfo.isTextUnderline)

        Utility.log("Add")

        return dataBase.insert("my_bible_text_info_table", null, cv) //Добавляем информацию о тексте и возвращаем id, под которым эти данные добавлены в БД
    }

    fun updateBibleTextInfo(highlightedBibleTextInfo: HighlightedBibleTextInfoModel) {
        cv.put("book_number", highlightedBibleTextInfo.bookNumber)
        cv.put("translation_name", highlightedBibleTextInfo.translationName)
        cv.put("chapter_number", highlightedBibleTextInfo.chapterNumber)
        cv.put("verse_number", highlightedBibleTextInfo.verseNumber)
        cv.put("text_color_hex", highlightedBibleTextInfo.textColorHex)
        cv.put("text_bold", highlightedBibleTextInfo.isTextBold)
        cv.put("text_underline", highlightedBibleTextInfo.isTextUnderline)

        Utility.log("Update")

        dataBase.update("my_bible_text_info_table", cv, "id = ?", arrayOf(highlightedBibleTextInfo.id.toString()))
    }

    fun deleteBibleTextInfo(bibleTextInfoId: Long) {
        Utility.log("Delete")

        dataBase.delete("my_bible_text_info_table", "id = $bibleTextInfoId", null)
    }

    //Метод для проверки элемента на наличие в БД
    fun isBibleTextInfoInDB(textId: Int): Boolean {
        val cursor: Cursor = dataBase.rawQuery("SELECT * FROM my_bible_text_info_table where id= $textId", null)
        if (cursor.count > 0) {
            cursor.close()
            return true
        }
        cursor.close()
        return false
    }

    fun closeDatabase() {
        dbHelp.close()
    }

    private inner class DBHelp(context: Context) : SQLiteOpenHelper(context, BIBLE_TEXT_INFO_BASE_NAME, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            Utility.log("DataBase created")
            // Создаем таблицу с полями
            db.execSQL("create table my_bible_text_info_table ("
                    + "id integer primary key autoincrement,"
                    + "book_number integer,"
                    + "translation_name text,"
                    + "chapter_number integer,"
                    + "verse_number integer,"
                    + "text_color_hex text,"
                    + "text_bold integer,"
                    + "text_underline integer" + ");")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
}
