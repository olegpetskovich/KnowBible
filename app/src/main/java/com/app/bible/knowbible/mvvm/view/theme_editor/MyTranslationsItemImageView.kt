package com.app.bible.knowbible.mvvm.view.theme_editor

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat

class MyTranslationsItemImageView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr),
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
        ImageViewCompat.setImageTintList(this, ContextCompat.getColorStateList(context, theme.colorViewForTranslationsItemTheme.color))

        //Этот код нужен для отображения анимации нажатия на иконку
//        val outValue = TypedValue()
//        context.theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true)
//        setBackgroundResource(outValue.resourceId)
    }
}
