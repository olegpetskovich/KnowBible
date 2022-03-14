package com.app.bible.knowbible.mvvm.view.fragment.more_section

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.app.bible.knowbible.R
import com.app.bible.knowbible.data.local.HighlightedBibleTextInfoDBHelper
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabMoreNumber
import com.app.bible.knowbible.mvvm.view.adapter.HighlightedVersesListRVAdapter
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleRootFragment
import com.app.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.app.bible.knowbible.utility.SaveLoadData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_bible_text.*
import kotlinx.android.synthetic.main.item_bible_translation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class HighlightedVersesFragment : Fragment(), IChangeFragment {

    private lateinit var listener: IActivityCommunicationListener
    private lateinit var saveLoadData: SaveLoadData

    private lateinit var myFragmentManager: FragmentManager
    private lateinit var bibleDataViewModel: BibleDataViewModel

    private lateinit var rvAdapter: HighlightedVersesListRVAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var animationView: LottieAnimationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView = inflater.inflate(R.layout.fragment_highlighted_verses, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение
        saveLoadData = SaveLoadData(requireContext())

        animationView = myView.findViewById(R.id.animationView)

        progressBar = myView.findViewById(R.id.progressBar)

        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!
        swipeRefreshLayout = myView.findViewById(R.id.swipeRefreshLayout)
        recyclerView = myView.findViewById(R.id.recyclerView)

        return myView
    }

    @SuppressLint("CheckResult")
    private fun getHighlightedVerses() {
        progressBar.visibility = View.VISIBLE
        HighlightedBibleTextInfoDBHelper
                .getInstance(context)!!
                .loadBibleTextInfo(-1, (activity as MainActivity).tvSelectTranslation.text.toString()) /*Берём обозначение перевода Библии из поля btnSelectTranslation, потому что оно уникально для каждого перевода и позволяет обозначить к какому переводу принадлежит выделяемый текст*/
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { highlightedTextsInfoList ->
                    if (highlightedTextsInfoList.size == 0) {
                        animationView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        progressBar.visibility = View.GONE
                        swipeRefreshLayout.isRefreshing = false
                        return@subscribe
                    }

                    bibleDataViewModel
                            .getHighlightedBibleVerse(BibleDataViewModel.TABLE_VERSES, highlightedTextsInfoList)
                            .observe(viewLifecycleOwner, { highlightedVersesList ->
                                highlightedVersesList?.reverse()
                                rvAdapter = highlightedVersesList?.let { HighlightedVersesListRVAdapter(requireContext(), it, myFragmentManager) }!!
                                rvAdapter.setFragmentChangerListener(this)
                                recyclerView.visibility = View.VISIBLE
                                recyclerView.layoutManager = LinearLayoutManager(context)
                                recyclerView.adapter = rvAdapter
                                swipeRefreshLayout.isRefreshing = false //Почему-то не плавно срабатывает перезагрузка

                                animationView.visibility = View.GONE
                                progressBar.visibility = View.GONE
                            })
                }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Устанавливаем нужный layout на отображаемую ориентацию экрана. Делать это по той причине, что обновление активити отключено при повороте экрана,
        //поэтому в случае необходимсти обновления xml, это нужно делать самому
        myFragmentManager.let {
            val themeFragment = HighlightedVersesFragment()
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

        //При возвращении на этот фрагмент автоматически обновляем список
        val mainHandler = Handler(requireContext().mainLooper)
        val myRunnable = Runnable {
            GlobalScope.launch(Dispatchers.Main) {
                delay(350)

                getHighlightedVerses()

                swipeRefreshLayout.setOnRefreshListener { getHighlightedVerses() }
            }
        }
        mainHandler.post(myRunnable)

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

    override fun changeFragment(fragment: Fragment) {
        //Чтобы можно было открыть фрагмент из другого Таба, для этого нужно использовать FragmentManager другого таба, что мы и делаем здесь,
        //вызывая статическую переменную BIBLE_FRAGMENT_MANAGER того Таба, в который нам нужно перейти
        //Иначе произойдёт ошибка
        BibleRootFragment.BIBLE_FRAGMENT_MANAGER.let {
            val myFragment = fragment as BibleTextFragment
            myFragment.setRootFragmentManager(it)

            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragment_container_bible, myFragment)
            transaction.commit()

            Handler().post { (activity as MainActivity).viewPager.currentItem = 1 } //Метод currentItem работает корректно именно облачённый в Handler
        }
    }
}