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
import com.airbnb.lottie.LottieAnimationView
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabMoreNumber
import com.app.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.dialog.DonateDetailsDialog
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utility.Companion.pulsatingAnimation

class SupportMinistryFragment : Fragment(), DialogListener {

    private lateinit var listener: IActivityCommunicationListener
    private lateinit var saveLoadData: SaveLoadData

    private lateinit var myFragmentManager: FragmentManager
    private lateinit var donateDetailsDialog: DonateDetailsDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_donation, container, false)
        listener.setTheme(
            ThemeManager.theme,
            false
        ) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(requireContext())

//        val donationAnimationView: LottieAnimationView = myView.findViewById(R.id.donationAnimationView)
//        donationAnimationView.setMaxFrame(60) //Устанавливаем значение 50 из 100, то есть остановить анимацию на половину

//        val btnDonateDetails: MaterialButton = myView.findViewById(R.id.btnVerseDetails)
//        btnDonateDetails.setOnClickListener {
//            donateDetailsDialog = DonateDetailsDialog(this)
//            donateDetailsDialog.isCancelable = true
//            donateDetailsDialog.show(childFragmentManager, "Donate Details Dialog") //Тут должен быть именно childFragmentManager
//        }

        val btnSupportMinistry: View = myView.findViewById(R.id.btnSupportMinistry)
        pulsatingAnimation(btnSupportMinistry)
        btnSupportMinistry.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.support_know_bible_link))
                )
            )
        }

        return myView
    }

    //В данном случае не нужно обновлять фрагмент, тут все данные статичные, если же обновлять фрагмент, то будут сбивать данные и настройки, которые сбивать не нужно
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Устанавливаем нужный layout на отображаемую ориентацию экрана. Делать это по той причине, что обновление активити отключено при повороте экрана,
        //поэтому в случае необходимсти обновления xml, это нужно делать самому
        myFragmentManager.let {
//            if (donateDetailsDialog.dialog != null && donateDetailsDialog.dialog!!.isShowing) {
//                donateDetailsDialog.dismiss()
//            }
//            donateDetailsDialog = DonateDetailsDialog(this)
//            donateDetailsDialog.isCancelable = true
//            donateDetailsDialog.show(childFragmentManager, "Donate Details Dialog") //Тут должен быть именно childFragmentManager

//            val themeFragment = DonateFragment()
//            themeFragment.setRootFragmentManager(it)
//            val transaction: FragmentTransaction = it.beginTransaction()
//            transaction.replace(R.id.fragment_container_more, themeFragment)
//            transaction.commit()
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
        listener.setBtnDonationClickableState(false) //Отключаем возможность нажимать на кнопку, чтобы нельзя было открывать фрагмент постоянно. Устанавливать нужно именно в методе onResume, чтобы в случае, когда приложение выводится из свёрнутого, кнопка снова отключилась.

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)
    }

    override fun onStop() {
        super.onStop()
        listener.setBtnDonationClickableState(true) //Включаем возможность нажимать на кнопку
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun dismissDialog() {
        donateDetailsDialog.dismiss()
    }
}