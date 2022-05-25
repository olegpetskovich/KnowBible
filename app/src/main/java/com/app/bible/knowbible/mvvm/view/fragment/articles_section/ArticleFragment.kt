package com.app.bible.knowbible.mvvm.view.fragment.articles_section

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabArticlesNumber
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.app.bible.knowbible.utility.Utils

class ArticleFragment : Fragment() {
    private lateinit var listener: IActivityCommunicationListener

    private lateinit var webView: WebView

    private lateinit var myFragmentManager: FragmentManager

    private lateinit var articleLink: String

    fun setArticleData(articleLink: String) {
        this.articleLink = articleLink
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_article, container, false)
        listener.setTheme(
            ThemeManager.theme,
            false
        ) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        val progressBar: ProgressBar = myView.findViewById(R.id.progressBar)

        val swipeRefresh: SwipeRefreshLayout = myView.findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { loadUrl(articleLink) }

        webView = myView.findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                return if (url == null || url.startsWith("http://") || url.startsWith("https://")) false else try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    view.context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.isVisible = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.isVisible = false
                swipeRefresh.isRefreshing = false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                progressBar.progress = progress
            }
        }

        loadUrl(articleLink)

        return myView
    }

    private fun showText(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun showText(@StringRes res: Int) {
        showText(getString(res))
    }

    private fun loadUrl(url: String) {
        if (!Utils.isNetworkAvailable(requireContext())) {
            showText(R.string.toast_no_internet_connection)
            return
        }
        webView.loadUrl(url)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Устанавливаем нужный layout на отображаемую ориентацию экрана. Делать это по той причине, что обновление активити отключено при повороте экрана,
        //поэтому в случае необходимсти обновления xml, это нужно делать самому
        myFragmentManager.let {
            val articleFragment = ArticleFragment()
            articleFragment.setRootFragmentManager(it)
            articleFragment.articleLink = articleLink

            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_articles, articleFragment)
            transaction.commit()
        }
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
        listener.setTabNumber(tabArticlesNumber)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.VISIBLE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)
    }
}