package ca.justinmo.lumiere.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.TextView
import android.widget.Toolbar
import ca.justinmo.lumiere.R
import java.time.Year

class AboutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
        
        window.statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

        setContentView(R.layout.about_activity)

        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: ""
        val appDescription = intent.getStringExtra(EXTRA_APP_DESCRIPTION) ?: ""

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        toolbar.setOnApplyWindowInsetsListener { view, insets ->
            val top = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets.getInsets(WindowInsets.Type.systemBars()).top
            } else {
                @Suppress("DEPRECATION")
                insets.systemWindowInsetTop
            }
            view.setPadding(view.paddingLeft, top, view.paddingRight, view.paddingBottom)
            insets
        }

        val scrollContent = findViewById<View>(R.id.scroll_content)
        scrollContent.setOnApplyWindowInsetsListener { view, insets ->
            val bottom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets.getInsets(WindowInsets.Type.systemBars()).bottom
            } else {
                @Suppress("DEPRECATION")
                insets.systemWindowInsetBottom
            }
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottom)
            insets
        }

        val txtAppName = findViewById<TextView>(R.id.txt_app_name)
        val txtVersion = findViewById<TextView>(R.id.txt_version)
        val txtDescription = findViewById<TextView>(R.id.txt_description)
        val btnShare = findViewById<Button>(R.id.btn_share)
        val txtCopyright = findViewById<TextView>(R.id.txt_copyright)

        txtAppName.text = appName
        
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName
        txtVersion.text = getString(R.string.version_label, versionName)
        
        txtDescription.text = appDescription

        val currentYear = Year.now().value
        val developerName = "Justin Michaud-Ouellette"
        val copyrightText = "© $currentYear $developerName"
        
        val spannableCopyright = SpannableString(copyrightText)
        val start = copyrightText.indexOf(developerName)
        if (start != -1) {
            val end = start + developerName.length
            
            spannableCopyright.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://justinmo.ca"))
                    startActivity(browserIntent)
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            
            spannableCopyright.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        
        txtCopyright.text = spannableCopyright
        txtCopyright.movementMethod = LinkMovementMethod.getInstance()

        val storeUrl = "https://f-droid.org/packages/$packageName/"
        val shareMessage = getString(R.string.share_message_template, appName, storeUrl)

        btnShare.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareMessage)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    companion object {
        const val EXTRA_APP_NAME = "EXTRA_APP_NAME"
        const val EXTRA_APP_DESCRIPTION = "EXTRA_APP_DESCRIPTION"
    }
}
