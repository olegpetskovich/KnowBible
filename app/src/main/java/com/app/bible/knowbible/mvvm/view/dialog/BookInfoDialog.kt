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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.text.HtmlCompat
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.model.BookModel
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.dialog_book_info.*


class BookInfoDialog(private val listener: DialogListener, private val bookInfo: BookModel) : AppCompatDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_book_info, null)

        //По непонятной причине в диалогах тема не меняется, поэтому приходится менять их в каждом диалоге
        when (SaveLoadData(requireContext()).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> ThemeManager.theme = ThemeManager.Theme.LIGHT
            ThemeModeFragment.DARK_THEME -> ThemeManager.theme = ThemeManager.Theme.DARK
            ThemeModeFragment.BOOK_THEME -> ThemeManager.theme = ThemeManager.Theme.BOOK
        }

        val btnDismissDialog: MaterialButton = view.findViewById(R.id.btnDismissDialog)
        btnDismissDialog.setOnClickListener { listener.dismissDialog() }

        val tvBookInfo: TextView = view.findViewById(R.id.tvBookInfo)
        //Достаём string res с помощью имени поля в res. Имя для поля получаем через конкатенацию фразы book_description_ и номера книги.
        //Получив строку, конвертируем её с html текста в обычный текст
        val resourceIdBookText = activity?.resources?.getIdentifier("book_description_${bookInfo.book_number}", "string", activity?.applicationContext?.packageName)
        resourceIdBookText?.let {
            tvBookInfo.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SpannableString(Html.fromHtml(requireContext().getString(it), HtmlCompat.FROM_HTML_MODE_LEGACY))
            } else {
                SpannableString(Html.fromHtml(requireContext().getString(it)))
            }
        }

        val bookIcon: ImageView = view.findViewById(R.id.bookIcon)
        val resourceIdBookIcon = activity?.resources?.getIdentifier("book_${bookInfo.book_number}", "drawable", activity?.applicationContext?.packageName)
        resourceIdBookIcon?.let { bookIcon.setImageResource(it) }

        builder.setView(view)
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
