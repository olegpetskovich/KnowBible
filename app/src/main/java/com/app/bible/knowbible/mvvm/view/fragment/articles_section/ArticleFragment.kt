package com.app.bible.knowbible.mvvm.view.fragment.articles_section

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.app.bible.knowbible.R
import com.app.bible.knowbible.mvvm.view.activity.MainActivity.Companion.tabArticlesNumber
import com.app.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.app.bible.knowbible.mvvm.view.theme_editor.ThemeManager

class ArticleFragment : Fragment() {
    private lateinit var listener: IActivityCommunicationListener

    private lateinit var myFragmentManager: FragmentManager

    private lateinit var articleImage: Bitmap
    private lateinit var articleName: String
    private lateinit var articleText: String
    private lateinit var authorName: String
    private lateinit var telegramLink: String
    private lateinit var instagramLink: String

    fun setArticleData(articleImage: Bitmap, articleName: String, articleText: String, authorName: String, telegramLink: String, instagramLink: String) {
        this.articleImage = articleImage
        this.articleName = articleName
        this.articleText = articleText
        this.authorName = authorName
        this.telegramLink = telegramLink
        this.instagramLink = instagramLink
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView = inflater.inflate(R.layout.fragment_article, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение

        val ivArticleImage: ImageView = myView.findViewById(R.id.ivArticleImage)
        ivArticleImage.setImageBitmap(articleImage)

        val tvArticleName: TextView = myView.findViewById(R.id.tvArticleName)
        tvArticleName.text = articleName

        val tvArticleText: TextView = myView.findViewById(R.id.tvArticleText)
        tvArticleText.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SpannableString(Html.fromHtml(articleText, HtmlCompat.FROM_HTML_MODE_LEGACY))
        } else {
            SpannableString(Html.fromHtml(articleText))
        }

        val tvAuthorName: TextView = myView.findViewById(R.id.tvAuthorName)
        tvAuthorName.text = authorName

        val btnTelegramChannel: ImageView = myView.findViewById(R.id.btnTelegramChannel)
        if (telegramLink.isEmpty()) {
            btnTelegramChannel.visibility = View.GONE
        } else {
            btnTelegramChannel.visibility = View.VISIBLE
            btnTelegramChannel.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, telegramLink.toUri())) }
        }

        val btnInstagramChannel: ImageView = myView.findViewById(R.id.btnInstagramChannel)
        if (instagramLink.isEmpty()) {
            btnInstagramChannel.visibility = View.GONE
        } else {
            btnInstagramChannel.visibility = View.VISIBLE
            btnInstagramChannel.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, instagramLink.toUri())) }
        }

        myFragmentManager.let {
            //            val transaction: FragmentTransaction = it.beginTransaction()
//            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
//
//
//            transaction.replace(R.id.fragment_container_more, )
//            transaction.addToBackStack(null)
//            transaction.commit()
        }
        return myView
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Устанавливаем нужный layout на отображаемую ориентацию экрана. Делать это по той причине, что обновление активити отключено при повороте экрана,
        //поэтому в случае необходимсти обновления xml, это нужно делать самому
        myFragmentManager.let {
            val articleFragment = ArticleFragment()
            articleFragment.setRootFragmentManager(it)
            articleFragment.articleImage = articleImage
            articleFragment.articleName = articleName
            articleFragment.articleText = articleText
            articleFragment.authorName = authorName

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