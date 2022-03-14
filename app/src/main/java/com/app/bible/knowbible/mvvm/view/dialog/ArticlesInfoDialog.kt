package com.app.bible.knowbible.mvvm.view.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.google.android.material.button.MaterialButton

class ArticlesInfoDialog(private val listener: DialogListener) : AppCompatDialogFragment(), DialogListener {

    private lateinit var articlesRegulationsDialog: ArticlesRegulationsDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        val inflater = LayoutInflater.from(context)
        val myView: View = inflater.inflate(R.layout.dialog_articles_info, null)

        //По непонятной причине в диалогах тема не меняется, поэтому приходится менять их в каждом диалоге
        when (SaveLoadData(requireContext()).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> ThemeManager.theme = ThemeManager.Theme.LIGHT
            ThemeModeFragment.DARK_THEME -> ThemeManager.theme = ThemeManager.Theme.DARK
            ThemeModeFragment.BOOK_THEME -> ThemeManager.theme = ThemeManager.Theme.BOOK
        }

        val btnTelegramChannel: ImageView = myView.findViewById(R.id.btnTelegramChannel)
        btnTelegramChannel.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.telegram_channel_link)))) }

        val btnInstagramChannel: ImageView = myView.findViewById(R.id.btnInstagramChannel)
        btnInstagramChannel.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.instagram_channel_link)))) }

        val btnDismissDialog: MaterialButton = myView.findViewById(R.id.btnDismissDialog)
        btnDismissDialog.setOnClickListener { listener.dismissDialog() }

        val lay = myView.findViewById<LinearLayout>(R.id.mainLayout)
        val tvArticleInfo = myView.findViewById<TextView>(R.id.tvArticleInfo)
        val btnRegulations = myView.findViewById<ImageView>(R.id.btnRegulations)
        btnRegulations.setOnClickListener {
            articlesRegulationsDialog = ArticlesRegulationsDialog(this)
            articlesRegulationsDialog.isCancelable = true
            articlesRegulationsDialog.show(childFragmentManager, "Articles Regulations Dialog") //Тут должен быть именно childFragmentManager
        }

        val linksLay = myView.findViewById<LinearLayout>(R.id.linksLay)

        //Весь этот код необходим потому, что в Книжной теме непонятно почему высота устанавливается на всю высоту экрана, даже если общая высота всех View намного меньше.
        //Из-за этого на экране получается огромный пробел.
        //Поскольку параметры View такие как высота, ширина и т.д. можно получить именно тогда, когда они уже отрисованы, необходимо их запрашивать именно в этот момент.
        //Поэтому мы используем обработчик изменения layout, которые позволяет нам получить необходимые параметры тогда, когда все View уже отрисованы.
        lay.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                lay.removeOnLayoutChangeListener(this) //На stackOverflow советуют удалять обработчик после первого вызова.
                //В этих двух полях собираем вместе параметры, которые дадут общую нужную высоту layout, которая будет установлена далее в mainLayout.layoutParams
                val layHeightValue = tvArticleInfo.height + btnRegulations.height + linksLay.height + btnDismissDialog.height + 50
                lay.layoutParams.height = layHeightValue
                lay.requestLayout()
            }
        })

        builder.setView(myView)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showHideBlurBackground(View.VISIBLE) //Делаем фон размытым
        //Устанавливаем закругленные края диалогу, ещё одна обязательная строка находится перед вызовом super.onCreate(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_corners)
        dialog?.window?.setDimAmount(0.5f) //Устанавливаем уровень тени на фоне
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).showHideBlurBackground(View.GONE) //Убираем размытый фон
    }

    override fun dismissDialog() {
        articlesRegulationsDialog.dismiss()
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
