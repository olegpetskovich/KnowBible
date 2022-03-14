package com.app.bible.knowbible.mvvm.model

import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager

data class ThemeInfoModel(val btnTextColor: String,
                          val btnBackgroundColor: Int,
                          val nameColorRes: String,
                          val nameRes: Int,
                          val backgroundRes: Int,
                          val themeAnimationRes: Int,
                          val speedAnimation: Float,
                          val cardBackgroundColorRes: Int,
                          val theme: ThemeManager.Theme)