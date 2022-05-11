package com.app.bible.knowbible.mvvm.view.fragment.more_section

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.TapTargetMoreFragment
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabMoreNumber
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utils
import com.getkeepsafe.taptargetview.TapTargetSequence
import kotlinx.android.synthetic.main.fragment_more.*

class MoreFragment : Fragment() {
    private lateinit var listener: IActivityCommunicationListener
    private lateinit var saveLoadData: SaveLoadData

    private lateinit var myFragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_more, container, false)
        listener.setTheme(
            ThemeManager.theme,
            false
        ) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        saveLoadData = SaveLoadData(requireContext())

        myFragmentManager.let {
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )

            val btnSettings: RelativeLayout = myView.findViewById(R.id.btnSettings)
            btnSettings.setOnClickListener {
                val settingsFragment = SettingsFragment()
                settingsFragment.setRootFragmentManager(myFragmentManager)

                transaction.replace(R.id.fragment_container_more, settingsFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            val btnHighlightedVerses: RelativeLayout =
                myView.findViewById(R.id.btnHighlightedVerses)
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
    }
}