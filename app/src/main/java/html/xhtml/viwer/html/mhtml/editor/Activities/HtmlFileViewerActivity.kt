package html.xhtml.viwer.html.mhtml.editor.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebSettings
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.webviewtopdf.PdfView
import html.xhtml.viwer.html.mhtml.editor.R
import html.xhtml.viwer.html.mhtml.editor.databinding.ActivityHtmlFileViewerBinding
import java.io.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class HtmlFileViewerActivity : AppCompatActivity() {

    lateinit var binding: ActivityHtmlFileViewerBinding

    var renamePdf = ""
    var message: String? = ""
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHtmlFileViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.extras
        try {
            message = bundle!!.getString("path")
            Log.d("TAG", "onActivityResult: " + message)
            val file = File(message!!)
            val settings: WebSettings = binding.xmlWebview.settings
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            binding.xmlWebview.loadUrl("file:///" + file.absolutePath)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        binding.viewCodeBtn.setOnClickListener {
            val intent = Intent(this@HtmlFileViewerActivity, ViewCodeActivity::class.java)
            intent.putExtra("file", message)
            startActivity(intent)
        }
        binding.pdfConvertBtn.setOnClickListener {
            show_alert_dialog()
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    fun show_alert_dialog() {
        try {
            val pathh = createPackageDirectory(this, "/PDF Files")
            val alert = Dialog(this)
            alert.setCancelable(false)
            alert.requestWindowFeature(Window.FEATURE_NO_TITLE)
            alert.setContentView(R.layout.dialog_rename)
            alert.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alert.getWindow()!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            );
            val fileName = alert.findViewById<EditText>(R.id.fileNameTV)
            val cancelBtn = alert.findViewById<TextView>(R.id.cancelBtn)
            val okBtn = alert.findViewById<TextView>(R.id.okBtn)
//
            //  fileName.setText(newString)
            okBtn.setOnClickListener {

                renamePdf = fileName.text.toString()
                val filee = File("$pathh/$renamePdf.pdf")
                val builder = null
                binding.convertprogressbar.visibility = View.VISIBLE
                PDFConversion(renamePdf)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    createPDF(renamePdf)
//                }
                alert.dismiss()
                // createPdf(this, builder.toString(), filee.absolutePath)

            }
            cancelBtn.setOnClickListener {
                alert.dismiss()
            }
            alert.show()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun PDFConversion(fileName:String){
        val path = createPackageDirectory(this, "/PDF Files")
        val directory=File(path)

        PdfView.createWebPrintJob(
            this@HtmlFileViewerActivity,
            binding.xmlWebview,
            directory,
            fileName+".pdf",
            object : PdfView.Callback {
                override fun success(path: String) {
                    binding.convertprogressbar.visibility=View.GONE
                    val snackbar = Snackbar.make(
                        findViewById(R.id.mylayout),
                        "Converted Successfully",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.show()
                    startActivity(Intent(this@HtmlFileViewerActivity,ConvertedFilesActivity::class.java))
                }

                override fun failure() {
                    Toast.makeText(this@HtmlFileViewerActivity, "Unable to create PDF!", Toast.LENGTH_SHORT).show()
                }
            })
    }


    fun createPackageDirectory(context: Context, dirName: String?): String? {
        val path = context.getExternalFilesDir(dirName).toString()
        val folder = File(path)
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.e("DirectoryLog :: ", "Problem creating folder")
            }
        }
        Log.e("FT :: ", "creating folder" + folder.getPath())
        return folder.getPath()
    }

//    @RequiresApi(Build.VERSION_CODES.R)
//    fun createPDF(renamePdf: String) {
//        // check permission is Granting
//        //write create pdf code here
//        val path = createPackageDirectory(this, "/PDF Files")
//        var file = File(path)
//
//
//        PdfView.createWebPrintJob(
//            this@HtmlFileViewerActivity,
//            binding.xmlWebview!!,
//            file,
//            renamePdf
//        ) {
//            Toast.makeText(this@HtmlFileViewerActivity, "Created!", Toast.LENGTH_SHORT).show()
//        }
//
//
//    }


}