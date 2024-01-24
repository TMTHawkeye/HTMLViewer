package html.xhtml.viwer.html.mhtml.editor.Activities

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import com.json.file.opener.json.reader.utility.FileUtil
import html.xhtml.viwer.html.mhtml.editor.BuildConfig
import html.xhtml.viwer.html.mhtml.editor.R
import html.xhtml.viwer.html.mhtml.editor.databinding.ActivityMainBinding
import io.paperdb.Paper
import java.io.BufferedReader
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var PICKFILE_REQUEST_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Paper.init(this)
        openNavDrawer()
        setNavContent()
        getTotalStorage()

        binding.cardHtmlViewer.setOnClickListener{
            startActivity(Intent(this, FilesListViewerActivity::class.java))
        }
        binding.cardCreateHtml.setOnClickListener{
            startActivity(Intent(this, CreateHtmlActivity::class.java))
        }
        binding.cardConvertedFiles.setOnClickListener{
            startActivity(Intent(this, ConvertedFilesActivity::class.java))
        }
        binding.cardRecentFiles.setOnClickListener{
            startActivity(Intent(this, RecentFilesActivity::class.java))
        }

        binding.pickFileBtn1.setOnClickListener {
            pickFile()
        }
    }

    private fun openNavDrawer() {
        binding.menuIcon.setOnClickListener(View.OnClickListener {
            binding.drawer.openDrawer(Gravity.LEFT)
            binding.drawer.setViewRotation(GravityCompat.START, 15f)
        })
    }

    private fun setNavContent() {
        binding.navView.setNavigationItemSelectedListener(
            { item ->
                when (item.itemId) {
                    R.id.rate_us -> try {
                        val marketUri = Uri.parse("market://details?id=$packageName")
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (e: ActivityNotFoundException) {
                        val marketUri =
                            Uri.parse("https://play.google.com/store/apps/dev?id=$packageName")
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    }
                    R.id.share -> try {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Test Your Lungs")
                        var shareMessage = "\nLet me recommend you this application\n\n"
                        shareMessage =
                            """
                    ${shareMessage}https://play.google.com/store/apps/dev?id=${BuildConfig.APPLICATION_ID}
                  
                    """.trimIndent()
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        startActivity(Intent.createChooser(shareIntent, "choose one"))
                    } catch (e: Exception) {
                    }
                    R.id.feedback -> {
                        val feedbackIntent = Intent(Intent.ACTION_SEND)
                        val recipients = arrayOf("smartianztech@gmail.com")
                        feedbackIntent.putExtra(Intent.EXTRA_EMAIL, recipients)
                        feedbackIntent.putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Feedback : " + R.string.app_name
                        )
                        feedbackIntent.type = "text/html"
                        feedbackIntent.setPackage("com.google.android.gm")
                        startActivity(Intent.createChooser(feedbackIntent, "Send mail"))
                    }
                    R.id.privacy -> {
                        val uri =
                            Uri.parse("https://smartianztech.blogspot.com/2023/01/welcome-to-smartianz-tech-inc.html")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    }
                    R.id.moreApps -> {
                        val moreAppUri =
                            Uri.parse("https://play.google.com/store/apps/developer?id=Smartianz+Tech")
                        val moreApp = Intent(Intent.ACTION_VIEW, moreAppUri)
                        startActivity(moreApp)
                    }
                    else -> {}
                }
                binding.drawer.closeDrawer(GravityCompat.START)
                true
            })
    }

    fun getTotalStorage(){
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val gb = (1024 * 1024 * 1024).toLong()
        val size = stat.totalBytes / gb
        val availableBlocks = stat.availableBytes.toLong()/gb
        Log.d("TAG", "getTotalStorage: "+size+"  "+availableBlocks)
        binding.totalStorageCapacity.text=size.toString()+" GB"
        binding.availableStorageCapacity.text=availableBlocks.toString()+" GB"

        binding.progressStorage.max=size.toInt()
        binding.progressStorage.progress=availableBlocks.toInt()
    }

    private fun pickFile() {
        val intent_file = Intent()
        val ACTIVITY_SELECT_File = 1234
        intent_file.action = Intent.ACTION_GET_CONTENT
        intent_file.type = "*/*"  //file of any type
        startActivityForResult(
            Intent.createChooser(intent_file, "Select File"),
            ACTIVITY_SELECT_File
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === PICKFILE_REQUEST_CODE && resultCode === RESULT_OK) {
            var reader: BufferedReader? = null
            try {
                var filePath = data!!.data!!

                var abs_path = FileUtil.from(this, filePath)
                if (abs_path.name.endsWith("html") || abs_path.name.endsWith("HTML") || abs_path.name.endsWith(
                        "Html"
                    )
                ) {
                    intentToViewer(abs_path.absolutePath)

                } else {
                    Toast.makeText(
                        this,
                        "Please choose a html file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun intentToViewer(path: String) {
        startActivity(
            Intent(
                this,
                HtmlFileViewerActivity::class.java
            ).putExtra("path", path)
        )
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
        } else
            popUp(this)
    }

    fun popUp(context: Activity) {
        val dialog = Dialog(context, R.style.MyAlertDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.backpress_dialogue)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent);
        val exitBtn: CardView = dialog.findViewById(R.id.exitbtn)
        val rateBtn: CardView = dialog.findViewById(R.id.ratenow)
        exitBtn.setOnClickListener { v: View? ->
            dialog.dismiss()
            context.finishAffinity()
        }
        rateBtn.setOnClickListener { v: View ->
            dialog.dismiss()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        }
        dialog.show()
    }



}