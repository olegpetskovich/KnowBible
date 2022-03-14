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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.text.HtmlCompat
import com.app.bible.knowbible.App
import com.app.bible.knowbible.App.Companion.EVENT_ADS_NO
import com.app.bible.knowbible.App.Companion.EVENT_ADS_YES
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.muddzdev.styleabletoast.StyleableToast

class AdsDialog : AppCompatDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_ads, null)

        //По непонятной причине в диалогах тема не меняется, поэтому приходится менять их в каждом диалоге
        when (SaveLoadData(requireContext()).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> ThemeManager.theme = ThemeManager.Theme.LIGHT
            ThemeModeFragment.DARK_THEME -> ThemeManager.theme = ThemeManager.Theme.DARK
            ThemeModeFragment.BOOK_THEME -> ThemeManager.theme = ThemeManager.Theme.BOOK
        }

        val btnNo: MaterialCardView = view.findViewById(R.id.btnNo)
        val btnYes: MaterialCardView = view.findViewById(R.id.btnYes)

        btnNo.setOnClickListener {
            App.instance.logToFirebase(EVENT_ADS_NO)
            dismissAndShowToast()
        }
        btnYes.setOnClickListener {
            App.instance.logToFirebase(EVENT_ADS_YES)
            dismissAndShowToast()
        }

        builder.setView(view)
        return builder.create()
    }

    private fun dismissAndShowToast() {
        SaveLoadData(requireContext()).saveBoolean("ads_question_key", true)
        dismiss()
        StyleableToast.makeText(
            requireContext(),
            getString(R.string.thanks_for_vote),
            Toast.LENGTH_LONG,
            R.style.my_toast
        ).show()
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
}
