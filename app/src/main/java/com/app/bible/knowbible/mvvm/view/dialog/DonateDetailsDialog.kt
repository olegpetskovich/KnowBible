package com.app.bible.knowbible.mvvm.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.text.HtmlCompat
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.google.android.material.button.MaterialButton

class DonateDetailsDialog(private val listener: DialogListener) : AppCompatDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_donate_details, null)

        //По непонятной причине в диалогах тема не меняется, поэтому приходится менять их в каждом диалоге
        when (SaveLoadData(requireContext()).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> ThemeManager.theme = ThemeManager.Theme.LIGHT
            ThemeModeFragment.DARK_THEME -> ThemeManager.theme = ThemeManager.Theme.DARK
            ThemeModeFragment.BOOK_THEME -> ThemeManager.theme = ThemeManager.Theme.BOOK
        }

        val myTextView: TextView = view.findViewById(R.id.myTextView)
        myTextView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SpannableString(Html.fromHtml(getString(R.string.tv_donate_details), HtmlCompat.FROM_HTML_MODE_LEGACY))
        } else {
            SpannableString(Html.fromHtml(getString(R.string.tv_donate_details)))
        }

        val btnDismissDialog: MaterialButton = view.findViewById(R.id.btnDismissDialog)
        btnDismissDialog.setOnClickListener { listener.dismissDialog() }
        builder.setView(view)

        //Весь этот код необходим потому, что в Книжной теме непонятно почему высота устанавливается на всю высоту экрана, даже если общая высота всех View намного меньше.
        //Из-за этого на экране получается огромный пробел.
        //Поскольку параметры View такие как высота, ширина и т.д. можно получить именно тогда, когда они уже отрисованы, необходимо их запрашивать именно в этот момент.
        //Поэтому мы используем обработчик изменения layout, которые позволяет нам получить необходимые параметры тогда, когда все View уже отрисованы.
//        val mainLay: LinearLayout = view.findViewById(R.id.mainLay)
//        mainLay.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
//            override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
//                mainLay.removeOnLayoutChangeListener(this) //На stackOverflow советуют удалять обработчик после первого вызова.
//                //В этих двух полях собираем вместе параметры, которые дадут общую нужную высоту layout, которая будет установлена далее в mainLayout.layoutParams
//                val layHeightValue = myTextView.height + btnDismissDialog.height + 50
//                mainLay.layoutParams.height = layHeightValue
//                mainLay.requestLayout()
//            }
//        })

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHideBlurBackground(View.VISIBLE) //Делаем фон размытым
        //Устанавливаем закругленные края диалогу, ещё одна обязательная строка находится перед вызовом super.onCreate(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_corners);
        dialog?.window?.setDimAmount(0.4f) //Устанавливаем уровень тени на фоне
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).showHideBlurBackground(View.GONE) //Убираем размытый фон
    }
    //Метод для связи с активити
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        listener = try {
//            context as LanguageDialogListener
//        } catch (e: ClassCastException) {
//            throw ClassCastException(context.toString() + "must implement LanguageDialogListener")
//        }
//    }
}
