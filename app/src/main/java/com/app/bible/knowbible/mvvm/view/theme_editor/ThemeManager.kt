package com.app.bible.knowbible.mvvm.view.theme_editor

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.app.bible.knowbible.R

object ThemeManager {
    private val listeners = mutableSetOf<ThemeChangedListener>()
    var theme = Theme.LIGHT
        set(value) {
            field = value
            listeners.forEach { listener -> listener.onThemeChanged(value) }
        }

    interface ThemeChangedListener {
        fun onThemeChanged(theme: Theme)
    }

    data class IconTheme(
            @ColorRes
            val tintColor: Int
    )

    data class ButtonTheme(
            @ColorRes
            val backgroundTint: Int,
            @ColorRes
            val rippleColor: Int,
            @ColorRes
            val strokeColor: Int,
            @ColorRes
            val iconColor: Int,
            @ColorRes
            val textColor: Int
    )

    data class CardViewTheme(
            @ColorRes
            val backgroundTint: Int,
            @ColorRes
            val rippleColor: Int,
            @ColorRes
            val strokeColor: Int
    )

    data class TextViewTheme(
            @ColorRes
            val textColor: Int
    )

    //Эти 2 нижних класса нужны для смены темы в item_bible_translation для тех TextView,
    //которым не подходит обычный класс MyTextView
    data class ColorViewForTranslationsItemTheme(
            @ColorRes
            val color: Int
    )

    data class TextViewForTranslationsItemTheme(
            @ColorRes
            val textColor: Int
    )

    data class ProgressBarTheme(
            @DrawableRes
            val progressBarColor: Int
    )

    data class ViewGroupTheme(
            @ColorRes
            val backgroundColor: Int
    )

    data class TabLayoutTheme(
            @ColorRes
            val backgroundColor: Int,
            @ColorRes
            val indicatorColor: Int,
            @ColorRes
            val selectedTextColor: Int,
            @ColorRes
            val textColor: Int
    )

    data class EditTextTheme(
            @ColorRes
            val textColor: Int,
            @ColorRes
            val hintColor: Int,
            @ColorRes
            val cursorColor: Int
    )

    data class RadioButtonTheme(
            @ColorRes
            val textColor: Int,
            @ColorRes
            val checkedColor: Int,
            @ColorRes
            val uncheckedColor: Int
    )

    data class ToolbarTheme(
            @ColorRes
            val backgroundColor: Int,
            val arrowColor: Int
    )

    data class ViewLineTheme(
            @ColorRes
            val backgroundColor: Int
    )

    data class BottomAppBarTheme(
            @ColorRes
            val backgroundColor: Int
    )

    data class FloatingActionButtonTheme(
            @ColorRes
            val backgroundColor: Int,
            @ColorRes
            val rippleColor: Int,
            @ColorRes
            val iconColor: Int
    )

    enum class Theme(
            val iconTheme: IconTheme,
            val buttonTheme: ButtonTheme,
            val cardViewTheme: CardViewTheme,
            val textViewTheme: TextViewTheme,
            val colorViewForTranslationsItemTheme: ColorViewForTranslationsItemTheme,
            val textViewForTranslationsItemTheme: TextViewForTranslationsItemTheme,
            val progressBarTheme: ProgressBarTheme,
            val viewGroupTheme: ViewGroupTheme,
            val tabLayoutTheme: TabLayoutTheme,
            val editTextTheme: EditTextTheme,
            val radioButtonTheme: RadioButtonTheme,
            val toolbarTheme: ToolbarTheme,
            val viewLineTheme: ViewLineTheme,
            val bottomAppBarTheme: BottomAppBarTheme,
            val floatingActionButtonTheme: FloatingActionButtonTheme
    ) {
        DARK(
                iconTheme = IconTheme(
                        tintColor = R.color.colorIconDarkTheme
                ),
                buttonTheme = ButtonTheme(
                        backgroundTint = R.color.colorButtonDarkTheme,
                        rippleColor = R.color.colorButtonRippleDarkTheme,
                        strokeColor = R.color.colorButtonStrokeDarkTheme,
                        iconColor = R.color.colorButtonIconDarkTheme,
                        textColor = R.color.colorTextDarkTheme
                ),
                cardViewTheme = CardViewTheme(
                        backgroundTint = R.color.colorButtonDarkTheme,
                        rippleColor = R.color.colorButtonRippleDarkTheme,
                        strokeColor = R.color.colorButtonStrokeDarkTheme
                ),
                textViewTheme = TextViewTheme(
                        textColor = R.color.colorTextDarkTheme
                ),
                colorViewForTranslationsItemTheme = ColorViewForTranslationsItemTheme(
                        color = R.color.colorPrimary
                ),
                textViewForTranslationsItemTheme = TextViewForTranslationsItemTheme(
                        textColor = R.color.colorGray
                ),
                progressBarTheme = ProgressBarTheme(
                        progressBarColor = R.drawable.circle_progress_bar
                ),
                viewGroupTheme = ViewGroupTheme(
                        backgroundColor = R.color.colorBackgroundDarkTheme
                ),
                tabLayoutTheme = TabLayoutTheme(
                        backgroundColor = R.color.colorBackgroundDarkTheme,
                        indicatorColor = R.color.colorTabIndicatorDarkTheme,
                        selectedTextColor = R.color.colorSelectedTabTextDarkTheme,
                        textColor = R.color.colorUnselectedTabTextDarkTheme
                ),
                editTextTheme = EditTextTheme(
                        R.color.colorTextDarkTheme,
                        R.color.colorGray,
                        R.color.colorIconCursorDarkTheme),
                radioButtonTheme = RadioButtonTheme(
                        textColor = R.color.colorTextDarkTheme,
                        checkedColor = R.color.colorCheckedLightDarkThemes,
                        uncheckedColor = R.color.colorUncheckedLightDarkThemes
                ),
                toolbarTheme = ToolbarTheme(
                        backgroundColor = R.color.colorBackgroundDarkTheme,
                        arrowColor = android.R.color.darker_gray
                ),
                viewLineTheme = ViewLineTheme(
                        backgroundColor = R.color.colorLineDarkTheme
                ),
                bottomAppBarTheme = BottomAppBarTheme(
                        backgroundColor = R.color.colorBackgroundDarkTheme
                ),
                floatingActionButtonTheme = FloatingActionButtonTheme(
                        backgroundColor = R.color.colorBackgroundFABDarkTheme,
                        rippleColor = R.color.colorRippleFABDarkTheme,
                        iconColor = R.color.colorIconFABDarkTheme
                )
        ),
        LIGHT(
                iconTheme = IconTheme(
                        tintColor = R.color.colorIconLightTheme
                ),
                buttonTheme = ButtonTheme(
                        backgroundTint = R.color.colorButtonLightTheme,
                        rippleColor = R.color.colorButtonRippleLightTheme,
                        strokeColor = R.color.colorButtonStrokeLightTheme,
                        iconColor = R.color.colorButtonIconLightTheme,
                        textColor = R.color.colorTextLightTheme
                ),
                cardViewTheme = CardViewTheme(
                        backgroundTint = R.color.colorButtonLightTheme,
                        rippleColor = R.color.colorButtonRippleLightTheme,
                        strokeColor = R.color.colorButtonStrokeLightTheme
                ),
                textViewTheme = TextViewTheme(
                        textColor = R.color.colorTextLightTheme
                ),
                colorViewForTranslationsItemTheme = ColorViewForTranslationsItemTheme(
                        color = R.color.colorPrimary
                ),
                textViewForTranslationsItemTheme = TextViewForTranslationsItemTheme(
                        textColor = R.color.colorGray
                ),
                progressBarTheme = ProgressBarTheme(
                        progressBarColor = R.drawable.circle_progress_bar
                ),
                viewGroupTheme = ViewGroupTheme(
                        backgroundColor = R.color.colorBackgroundLightTheme
                ),
                tabLayoutTheme = TabLayoutTheme(
                        backgroundColor = R.color.colorBackgroundLightTheme,
                        indicatorColor = R.color.colorTabIndicatorLightTheme,
                        selectedTextColor = R.color.colorSelectedTabTextLightTheme,
                        textColor = R.color.colorUnselectedTabTextLightTheme
                ),
                editTextTheme = EditTextTheme(
                        R.color.colorTextLightTheme,
                        R.color.colorGray,
                        R.color.colorIconCursorLightTheme),
                radioButtonTheme = RadioButtonTheme(
                        textColor = R.color.colorTextLightTheme,
                        checkedColor = R.color.colorCheckedLightDarkThemes,
                        uncheckedColor = R.color.colorUncheckedLightDarkThemes
                ),
                toolbarTheme = ToolbarTheme(
                        backgroundColor = R.color.colorBackgroundLightTheme,
                        arrowColor = android.R.color.darker_gray
                ),
                viewLineTheme = ViewLineTheme(
                        backgroundColor = R.color.colorLineLightTheme
                ),
                bottomAppBarTheme = BottomAppBarTheme(
                        backgroundColor = R.color.colorBackgroundLightTheme
                ),
                floatingActionButtonTheme = FloatingActionButtonTheme(
                        backgroundColor = R.color.colorBackgroundFABLightTheme,
                        rippleColor = R.color.colorRippleFABLightTheme,
                        iconColor = R.color.colorIconFABLightTheme
                )
        ),
        BOOK(
                iconTheme = IconTheme(
                        tintColor = R.color.colorIconLightTheme
                ),
                buttonTheme = ButtonTheme(
                        backgroundTint = R.color.colorButtonBookTheme,
                        rippleColor = R.color.colorButtonRippleBookTheme,
                        strokeColor = R.color.colorButtonStrokeBookTheme,
                        iconColor = R.color.colorButtonIconBookTheme,
                        textColor = R.color.colorTextLightTheme
                ),
                cardViewTheme = CardViewTheme(
                        backgroundTint = R.color.colorButtonBookTheme,
                        rippleColor = R.color.colorButtonRippleBookTheme,
                        strokeColor = R.color.colorButtonStrokeBookTheme
                ),
                textViewTheme = TextViewTheme(
                        textColor = R.color.colorTextBookTheme
                ),
                colorViewForTranslationsItemTheme = ColorViewForTranslationsItemTheme(
                        color = android.R.color.black
                ),
                textViewForTranslationsItemTheme = TextViewForTranslationsItemTheme(
                        textColor = R.color.colorGrayBookTheme
                ),
                progressBarTheme = ProgressBarTheme(
                        progressBarColor = R.drawable.circle_progress_bar_book_theme
                ),
                viewGroupTheme = ViewGroupTheme(
                        backgroundColor = R.color.colorBackgroundBookTheme
                ),
                tabLayoutTheme = TabLayoutTheme(
                        backgroundColor = R.color.colorBackgroundBookTheme,
                        indicatorColor = R.color.colorTabIndicatorBookTheme,
                        selectedTextColor = R.color.colorSelectedTabTextBookTheme,
                        textColor = R.color.colorUnselectedTabTextBookTheme
                ),
                editTextTheme = EditTextTheme(
                        R.color.colorTextBookTheme,
                        R.color.colorGrayBookTheme,
                        R.color.colorIconCursorBookTheme),
                radioButtonTheme = RadioButtonTheme(
                        textColor = R.color.colorTextBookTheme,
                        checkedColor = R.color.colorCheckedBookTheme,
                        uncheckedColor = R.color.colorUncheckedBookTheme
                ),
                toolbarTheme = ToolbarTheme(
                        backgroundColor = R.color.colorBackgroundBookTheme,
                        arrowColor = android.R.color.darker_gray
                ),
                viewLineTheme = ViewLineTheme(
                        backgroundColor = R.color.colorLineBookTheme
                ),
                bottomAppBarTheme = BottomAppBarTheme(
                        backgroundColor = R.color.colorBackgroundBookTheme
                ),
                floatingActionButtonTheme = FloatingActionButtonTheme(
                        backgroundColor = R.color.colorBackgroundFABBookTheme,
                        rippleColor = R.color.colorRippleFABDarkTheme,
                        iconColor = R.color.colorIconFABBookTheme
                )
        )
    }

    fun addListener(listener: ThemeChangedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ThemeChangedListener) {
        listeners.remove(listener)
    }
}
