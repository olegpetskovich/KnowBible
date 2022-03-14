package com.app.bible.knowbible.mvvm.model

data class HighlightedBibleTextInfoModel(var id: Long = -1,
                                         val bookNumber: Int,
                                         val translationName: String,
                                         val chapterNumber: Int,
                                         val verseNumber: Int,
                                         var textColorHex: String?,
                                         var isTextBold: Boolean,
                                         var isTextUnderline: Boolean,
                                         var selectedItem: Int = -1)