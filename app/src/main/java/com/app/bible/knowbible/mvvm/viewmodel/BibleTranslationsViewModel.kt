package com.app.bible.knowbible.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.BibleTranslationModel

//Наследуемся от AndroidViewModel, чтобы можно было пользоваться контекстом для string ресурсов
class BibleTranslationsViewModel(application: Application) : AndroidViewModel(application) {
    private val translationsListLiveData = MutableLiveData<ArrayList<BibleTranslationModel>>()

    fun getTranslationsList(): LiveData<ArrayList<BibleTranslationModel>> {
        translationsListLiveData.value = getTranslationsListData()
        return translationsListLiveData
    }

    private fun getTranslationsListData(): ArrayList<BibleTranslationModel> {
        val translationsList = ArrayList<BibleTranslationModel>()

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.russian_language_translation),
                getApplication<Application>().getString(R.string.syno_translation),
                "synodal_translation_SYNO.SQLite3", //Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "SYNO")) //Обозначение должно быть уникальным, поскольку с его помощью в БД выделенных текстов сохраняются стихи с указанием того, в каком переводе они были сохранены

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.russian_language_translation),
                getApplication<Application>().getString(R.string.nrt_translation),
                "new_russian_translation_NRT.SQLite3", //Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "NRT")) //Обозначение должно быть уникальным, поскольку с его помощью в БД выделенных текстов сохраняются стихи с указанием того, в каком переводе они были сохранены

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.ukrainian_language_translation),
                getApplication<Application>().getString(R.string.ubio_translation),
                "translation_ivan_ogienko_88_UBIO.SQLite3",//Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "UBIO")) //Обозначение должно быть уникальным, поскольку с его помощью в БД выделенных текстов сохраняются стихи с указанием того, в каком переводе они были сохранены

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.ukrainian_language_translation),
                getApplication<Application>().getString(R.string.umt_translation),
                "ukrainian_modern_translation_UMT.SQLite3",//Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "UMT")) //Обозначение должно быть уникальным, поскольку с его помощью в БД выделенных текстов сохраняются стихи с указанием того, в каком переводе они были сохранены

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.english_language_translation),
                getApplication<Application>().getString(R.string.kjv_translation),
                "king_james_version_translation_KJV.SQLite3",//Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "KJV")) //Обозначение должно быть уникальным, поскольку с его помощью в БД выделенных текстов сохраняются стихи с указанием того, в каком переводе они были сохранены

        translationsList.add(BibleTranslationModel(
                getApplication<Application>().getString(R.string.english_language_translation),
                getApplication<Application>().getString(R.string.asv_translation),
                "american_standard_version_ASV.SQLite3",//Имя файла обязательно должно соответствовать с именем файла на FB, в противном случае не удастаться скачать файл
                "ASV")) //Обозначение должно быть уникальным, поскольку с его помощью в БД выделенных текстов сохраняются стихи с указанием того, в каком переводе они были сохранены

        return translationsList
    }
}