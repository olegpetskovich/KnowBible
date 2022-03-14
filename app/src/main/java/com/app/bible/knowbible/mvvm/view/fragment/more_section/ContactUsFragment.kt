package com.app.bible.knowbible.mvvm.view.fragment.more_section

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.airbnb.lottie.LottieAnimationView
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabMoreNumber
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.google.android.material.button.MaterialButton


class ContactUsFragment : Fragment() {

    private lateinit var listener: IActivityCommunicationListener
    private lateinit var saveLoadData: SaveLoadData

    private lateinit var myFragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //layout должен быть организован так, как есть, чтобы анимация нажатия отображалась правильно. По этой причине там находятся одни вьюшки поверх других
        val myView = inflater.inflate(R.layout.fragment_contact_us, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(requireContext())

        val telegramBtn: MaterialButton = myView.findViewById(R.id.telegramBtn)
        telegramBtn.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.telegram_channel_link)))) }

        val telegramLottieAnim: LottieAnimationView = myView.findViewById(R.id.telegramLottieAnim)
        telegramLottieAnim.setMinFrame(38) //Устанавливаем значение 50 из 100, то есть остановить анимацию на половину
        telegramLottieAnim.setMaxFrame(58)

        val instagramBtn: MaterialButton = myView.findViewById(R.id.instagramBtn)
        instagramBtn.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.instagram_channel_link)))) }

        val instagramLottieAnim: LottieAnimationView = myView.findViewById(R.id.instagramLottieAnim)
        instagramLottieAnim.setMinFrame(20) //Устанавливаем значение 50 из 100, то есть остановить анимацию на половину
        instagramLottieAnim.setMaxFrame(50)

        val emailBtn: MaterialButton = myView.findViewById(R.id.emailBtn)
        emailBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.know_bible_help_mail), null))
            startActivity(Intent.createChooser(intent, ""))
        }

        val emailLottieAnim: LottieAnimationView = myView.findViewById(R.id.emailLottieAnim)
        emailLottieAnim.setMinFrame(36) //Устанавливаем значение 50 из 100, то есть остановить анимацию на половину
        emailLottieAnim.setMaxFrame(80)
        return myView
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Устанавливаем нужный layout на отображаемую ориентацию экрана. Делать это по той причине, что обновление активити отключено при повороте экрана,
        //поэтому в случае необходимсти обновления xml, это нужно делать самому
        myFragmentManager.let {
            val themeFragment = ContactUsFragment()
            themeFragment.setRootFragmentManager(it)
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_more, themeFragment)
            transaction.commit()
        }
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun onResume() {
        super.onResume()
        listener.setTabNumber(tabMoreNumber)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }
}