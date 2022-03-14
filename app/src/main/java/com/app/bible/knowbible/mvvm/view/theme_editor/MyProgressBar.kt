package com.app.bible.knowbible.mvvm.view.theme_editor

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.core.content.ContextCompat

class MyProgressBar
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ProgressBar(context, attrs, defStyleAttr),
        ThemeManager.ThemeChangedListener {

    override fun onFinishInflate() {
        super.onFinishInflate()
        ThemeManager.addListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ThemeManager.addListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ThemeManager.removeListener(this)
    }

    override fun onThemeChanged(theme: ThemeManager.Theme) {
        progressDrawable = ContextCompat.getDrawable(context, theme.progressBarTheme.progressBarColor)
    }
}
