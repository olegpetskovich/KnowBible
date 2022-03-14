package com.app.bible.knowbible.utility

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.animation.ValueAnimator.ofInt
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.BibleTranslationModel
import com.getkeepsafe.taptargetview.TapTarget
import java.io.File

class Utility {
    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm.activeNetwork != null &&
                        with(cm.getNetworkCapabilities(cm.activeNetwork!!)) {
                            this?.hasTransport(TRANSPORT_WIFI) == true ||
                                    this?.hasTransport(TRANSPORT_CELLULAR) == true ||
                                    this?.hasTransport(TRANSPORT_VPN) == true
                        }
            } else
                cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        }


        //Проверка на то, скачан ли хоть один перевод или же папка, предназначенная для скачанных переводов, пуста
        fun isTranslationsDownloaded(context: Context): Boolean {
            val directory = File(context.getExternalFilesDir(context.getString(R.string.folder_name)).toString())
            val contents = directory.listFiles()
            return contents != null && contents.isNotEmpty()
        }

        //Проверка на то, скачан ли перевод, выбранный ранее, или же перевод удалён и в saveLoadData хранится имя скачанного файла, но его самого не существует.
        //Если эту проверку не осуществлять, то в случае удаления выбранного перевода, программа будет пытаться открыть его, но не сможет,
        //потому что в действительности он будет удалён
        fun isSelectedTranslationDownloaded(context: Context, bibleTranslationInfo: BibleTranslationModel): Boolean {
            val applicationFile = File(context.getExternalFilesDir(context.getString(R.string.folder_name)).toString() + "/" + bibleTranslationInfo.translationDBFileName)
            return applicationFile.exists()
        }

        fun log(text: String) {
            Log.d("MyTag", text)
        }

        fun log(tag: String, text: String) {
            Log.d(tag, text)
        }

        //Конвертируем вводное число DP в пиксели
        fun convertDpToPx(context: Context, dp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        }

        fun convertPxToDp(px: Float, context: Context): Float {
            return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun pulsatingAnimation(view: View) {
            val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f)
            )
            scaleDown.duration = 310
            scaleDown.interpolator = FastOutSlowInInterpolator()
            scaleDown.repeatCount = ObjectAnimator.INFINITE
            scaleDown.repeatMode = ObjectAnimator.REVERSE

            scaleDown.start()
        }

        fun viewAnimatorX(pixels: Float, view: View, myDuration: Long): ObjectAnimator? {
            return ObjectAnimator
                    .ofFloat(view, "translationX", pixels)
                    .apply {
                        duration = myDuration
                    }
        }

        fun viewAnimatorY(pixels: Float, view: View, myDuration: Long): ObjectAnimator? {
            return ObjectAnimator
                    .ofFloat(view, "translationY", pixels)
                    .apply {
                        duration = myDuration
                    }
        }

        fun getClearedStringFromTags(string: String): String {
            //Очистка текст от ненужных тегов и знаков с помощью "регулярных выражений"
//            val reg1 = Regex("""<S>(\d+)</S>""")
//            val reg1 = Regex("""<S>(.+)</S>""")
//            val reg1 = Regex("""<S>[\d\w\s]*</S>""")
            val reg1 = Regex("""<S>(.*?)</S>""") //Определяет всю строку с любым набором символов
            val reg2 = Regex("""<f>(\S+)</f>""")
            val reg3 = Regex("""<(\w)>|</(\w)>""") //Без удаления пробела

            var str = string
            str = str.replace(reg1, "")
            str = str.replace(reg2, "")
            str = str.replace(reg3, "")
            str = str.replace("<pb/>", "")
            str = str.replace("<br/>", "")
            str = str.replace("[1]", "")

            return str
        }

        fun getClearedText(sb: StringBuilder): String {
            if (sb[0] == ' ') sb.deleteCharAt(0)
            if (sb[sb.length - 1] == '.' || sb[sb.length - 1] == ',' || sb[sb.length - 1] == ';' || sb[sb.length - 1] == ' ') sb.deleteCharAt(sb.length - 1)
            return sb.toString()
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val view: View? = activity.currentFocus
            if (view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun slideView(view: View, duration: Int, currentHeight: Int, newHeight: Int, isExpand: Boolean): AnimatorSet {
            val slideAnimator = ofInt(currentHeight, newHeight).setDuration(duration.toLong())

            slideAnimator.addUpdateListener { animation1: ValueAnimator ->
                val value = animation1.animatedValue
                view.layoutParams.height = value as Int
                view.requestLayout()
            }

            val animationSet = AnimatorSet()
            if (isExpand) animationSet.interpolator = AccelerateInterpolator()
            else animationSet.interpolator = DecelerateInterpolator()
            animationSet.play(slideAnimator)
            return animationSet
        }

        //Метод для показа подсказок
        fun getTapTargetButton(view: View, context: Context, titleStringRes: Int, descriptionStringRes: Int, targetRadius: Int): TapTarget? {
            return TapTarget.forView(view, context.getString(titleStringRes), context.getString(descriptionStringRes)) // All options below are optional
                    .outerCircleColor(R.color.colorPrimary) // Specify a color for the outer circle
                    .outerCircleAlpha(0.98f) // Specify the alpha amount for the outer circle
//                                .targetCircleColor(android.R.color.white) // Specify a color for the target circle
                    .titleTextSize(20) // Specify the size (in sp) of the title text
                    .titleTextColor(android.R.color.white) // Specify the color of the title text
                    .descriptionTextSize(14) // Specify the size (in sp) of the description text
                    .descriptionTextColor(android.R.color.white) // Specify the color of the description text
//                                .textColor(R.color.colorPrimary) // Specify a color for both the title and description text
                    .textTypeface(Typeface.SANS_SERIF) // Specify a typeface for the text
                    .dimColor(android.R.color.black) // If set, will dim behind the view with 30% opacity of the given color
                    .drawShadow(true) // Whether to draw a drop shadow or not
                    .cancelable(true) // Whether tapping outside the outer circle dismisses the view
                    .tintTarget(true) // Whether to tint the target view's color
                    .transparentTarget(true) // Specify whether the target is transparent (displays the content underneath)
                    .targetRadius(targetRadius)
        }
    }
}