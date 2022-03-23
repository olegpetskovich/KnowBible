package com.app.bible.knowbible.mvvm.view.fragment.more_section

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.TapTargetMoreFragment
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabMoreNumber
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utility
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_more.*

class MoreFragment : Fragment() {
    private lateinit var listener: IActivityCommunicationListener
    private lateinit var saveLoadData: SaveLoadData

    private lateinit var myFragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView = inflater.inflate(R.layout.fragment_more, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(requireContext())

        myFragmentManager.let {
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)

            val btnSettings: RelativeLayout = myView.findViewById(R.id.btnSettings)
            btnSettings.setOnClickListener {
                val settingsFragment = SettingsFragment()
                settingsFragment.setRootFragmentManager(myFragmentManager)

                transaction.replace(R.id.fragment_container_more, settingsFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            val btnHighlightedVerses: RelativeLayout = myView.findViewById(R.id.btnHighlightedVerses)
            btnHighlightedVerses.setOnClickListener {
                val highlightedVersesFragment = HighlightedVersesFragment()
                highlightedVersesFragment.setRootFragmentManager(myFragmentManager)

                transaction.replace(R.id.fragment_container_more, highlightedVersesFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            val btnContactUs: RelativeLayout = myView.findViewById(R.id.btnContactUs)
            btnContactUs.setOnClickListener {
                val contactUsFragment = ContactUsFragment()
                contactUsFragment.setRootFragmentManager(myFragmentManager)

                transaction.replace(R.id.fragment_container_more, contactUsFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            val btnPrivacyPolicy: RelativeLayout = myView.findViewById(R.id.btnPrivacyPolicy)
            btnPrivacyPolicy.setOnClickListener {
                val privacyPolicyFragment = PrivacyPolicyFragment()
                privacyPolicyFragment.setRootFragmentManager(myFragmentManager)

                transaction.replace(R.id.fragment_container_more, privacyPolicyFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

//            val btnDonate: RelativeLayout = myView.findViewById(R.id.btnDonate)
//            btnDonate.setOnClickListener {
//                val donateFragment = DonateFragment()
//                donateFragment.setRootFragmentManager(myFragmentManager)
//
//                transaction.replace(R.id.fragment_container_more, donateFragment)
//                transaction.addToBackStack(null)
//                transaction.commit()
//            }
        }
        return myView
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onResume() {
        super.onResume()
        listener.setTabNumber(tabMoreNumber)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(false)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.GONE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)

        //Прописываем условие, чтобы этот код срабатывал только один раз
        if (!saveLoadData.loadBoolean(TapTargetMoreFragment)) {
            //Помещаем код в Handler, потому что только так можно получить значение параметров высоты и ширины
            val mainHandler = Handler(requireContext().mainLooper)
            val myRunnable =
                    Runnable {
                        val appBarLayout = (activity as MainActivity).findViewById<AppBarLayout>(R.id.appBarLayout)
                        //Открываем appBarLayout в случае, если он выключен
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) appBarLayout.setExpanded(true, false)
                        else appBarLayout.setExpanded(true, true) //Открываем appBarLayout при включении режима множественного выбора

//                        val donLay = (activity as MainActivity).findViewById<LinearLayout>(R.id.donationLay)
                        TapTargetSequence(activity)
                                .targets(
//                                        Utility.getTapTargetButton(donLay, requireContext(), R.string.btn_donation_title, R.string.btn_donation_description, Utility.convertPxToDp(donLay.width.toFloat(), requireContext()).toInt() - 60),
                                        Utility.getTapTargetButton(tvSettings, requireContext(), R.string.btn_settings_title, R.string.btn_settings_description, Utility.convertPxToDp(tvSettings.width.toFloat(), requireContext()).toInt()),
                                        Utility.getTapTargetButton(tvHighlightedVerses, requireContext(), R.string.btn_highlighted_verses_title, R.string.btn_highlighted_verses_description, Utility.convertPxToDp(tvHighlightedVerses.width.toFloat(), requireContext()).toInt() - 60),
                                        Utility.getTapTargetButton(tvContactUs, requireContext(), R.string.btn_contact_us_title, R.string.btn_contact_us_description, Utility.convertPxToDp(tvContactUs.width.toFloat(), requireContext()).toInt() - 60),
                                ).start()

                    }
            mainHandler.post(myRunnable)
            saveLoadData.saveBoolean(TapTargetMoreFragment, true)
        }
    }
}