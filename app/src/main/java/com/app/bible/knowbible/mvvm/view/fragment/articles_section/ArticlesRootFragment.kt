package com.app.bible.knowbible.mvvm.view.fragment.articles_section

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.app.bible.knowbible.App
import com.app.bible.knowbible.R
import com.google.android.gms.ads.AdView

class ArticlesRootFragment : Fragment() {
    private var banner: AdView? = null
    private lateinit var adViewContainer: FrameLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val myView: View = inflater.inflate(R.layout.fragment_container_articles, container, false)

        adViewContainer = myView.findViewById(R.id.adViewContainer)

        childFragmentManager.let {
            val articlesFragment = ArticlesFragment()
            articlesFragment.setRootFragmentManager(childFragmentManager)

            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_articles, articlesFragment)
            transaction.commit()
        }
        return myView
    }

    override fun onResume() {
        super.onResume()
        banner?.resume()

        if (adViewContainer.childCount == 0) {
            banner = AdView(requireContext())
            adViewContainer.addView(banner)
            App.instance.bannerAdLoader.loadBanner(requireActivity(), adViewContainer, banner!!)
        }
    }

    override fun onPause() {
        super.onPause()
        banner?.pause()
    }
}