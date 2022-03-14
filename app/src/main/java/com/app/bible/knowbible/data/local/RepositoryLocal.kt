package com.app.bible.knowbible.data.local

import android.database.sqlite.SQLiteDatabase
import com.app.bible.knowbible.mvvm.model.*
import com.app.bible.knowbible.mvvm.model.HighlightedBibleTextInfoModel
import io.reactivex.Single

class RepositoryLocal {
    private val dbBibleHelper = BibleTextDBHelper()

    fun openDatabase(dbPath: String): SQLiteDatabase {
        return dbBibleHelper.openDatabase(dbPath)
    }

    fun closeDatabase() {
        dbBibleHelper.closeDatabase()
    }

    fun getTestamentBooksList(tableName: String, isOldTestament: Boolean): Single<ArrayList<BookModel>> {
        return dbBibleHelper
                .loadTestamentBooksList(tableName, isOldTestament)
                .flatMap { response: ArrayList<BookModel> ->
                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<ArrayList<BookModel>> { return@fromCallable response }
                }
    }

    fun getAllBooksList(tableName: String): Single<ArrayList<BookModel>> {
        return dbBibleHelper
                .loadAllBooksList(tableName)
                .flatMap { response: ArrayList<BookModel> ->
                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<ArrayList<BookModel>> { return@fromCallable response }
                }
    }

    fun getChaptersList(tableName: String, bookNumber: Int): Single<ArrayList<ChapterModel>> {
        return dbBibleHelper
                .loadChaptersList(tableName, bookNumber)
                .flatMap { response: ArrayList<ChapterModel> ->
                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<ArrayList<ChapterModel>> { return@fromCallable response }
                }
    }

    fun getBibleTextOfChapter(tableName: String, bookNumber: Int, chapterNumber: Int): Single<ArrayList<BibleTextModel>> {
        return dbBibleHelper
                .loadBibleTextOfChapter(tableName, bookNumber, chapterNumber)
                .flatMap { response: ArrayList<BibleTextModel> ->
                    run {
                        //Очищаем текст от ненужных тегов. Эти действия называются регулярными выражениями
                        for (element in response) {
//                            val reg = Regex(pattern = """<(\w)>|</(\w)>[\s]?""") //С удалением пробела
                            val reg1 = Regex("""<S>(\d+)</S>""")
                            val reg2 = Regex("""<f>(\S+)</f>""")
                            val reg3 = Regex("""<(\w)>|</(\w)>""") //Без удаления пробела

                            var str = element.text
                            str = str.replace(reg1, "")
                            str = str.replace(reg2, "")
                            str = str.replace(reg3, "")
                            str = str.replace("<pb/>", "")
                            element.text = str
                        }
                    }

                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<ArrayList<BibleTextModel>> { return@fromCallable response }
                }
    }

    fun getBibleTextOfBook(tableName: String, bookNumber: Int): Single<ArrayList<ArrayList<BibleTextModel>>> {
        return dbBibleHelper
                .loadBibleTextOfBook(tableName, bookNumber)
                .flatMap { response: ArrayList<ArrayList<BibleTextModel>> ->

                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<ArrayList<ArrayList<BibleTextModel>>> { return@fromCallable response }
                }
    }

    fun getBibleTextOfAllBible(tableName: String): Single<ArrayList<BibleTextModel>> {
        return dbBibleHelper
                .loadAllBibleTexts(tableName)
                .flatMap { response: ArrayList<BibleTextModel> ->

                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<ArrayList<BibleTextModel>> { return@fromCallable response }
                }
    }

    fun getSearchedBibleVerses(tableName: String, searchingSection: Int, searchingText: String): Single<ArrayList<BibleTextModel>> {
        return dbBibleHelper
                .loadSearchedBibleVerse(tableName, searchingSection, searchingText)
                .flatMap { response: ArrayList<BibleTextModel> ->
                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<ArrayList<BibleTextModel>> { return@fromCallable response }
                }
    }

    fun getBibleVerse(tableName: String, bookNumber: Int, chapterNumber: Int, verseNumber: Int): Single<BibleTextModel> {
        return dbBibleHelper
                .loadBibleVerse(tableName, bookNumber, chapterNumber, verseNumber)
                .flatMap { response: BibleTextModel ->
                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<BibleTextModel> { return@fromCallable response }
                }
    }

    fun getHighlightedBibleVerseData(tableName: String, highlightedTextsInfoList: ArrayList<HighlightedBibleTextInfoModel>): Single<ArrayList<BibleTextModel>> {
        return dbBibleHelper
                .loadHighlightedBibleVerse(tableName, highlightedTextsInfoList)
                .flatMap { response: ArrayList<BibleTextModel> ->
                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<ArrayList<BibleTextModel>> { return@fromCallable response }
                }
    }

    fun getBibleVerseForDailyVerse(tableName: String, bookNumber: Int, chapterNumber: Int, versesNumbers: ArrayList<Int>): Single<DailyVerseModel> {
        return dbBibleHelper
                .loadDailyVerse(tableName, bookNumber, chapterNumber, versesNumbers)
                .flatMap { response: DailyVerseModel ->
                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<DailyVerseModel> { return@fromCallable response }
                }
    }

    fun getBookShortName(tableName: String, bookNumber: Int): Single<String> {
        return dbBibleHelper
                .loadBookShortName(tableName, bookNumber)
                .flatMap { response: String ->
                    //flatMap здесь используется, чтобы показать, что при получении данных, их можно сначала отредактировать как надо, а потом отправить дальше
                    return@flatMap Single.fromCallable<String> { return@fromCallable response }
                }
    }

    fun updateBibleTextInDB(bibleTextModel: BibleTextModel) {
        dbBibleHelper.updateBibleText(bibleTextModel)
    }
}