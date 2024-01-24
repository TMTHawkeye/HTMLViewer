package html.xhtml.viwer.html.mhtml.editor.Activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import html.xhtml.viwer.html.mhtml.editor.R
import html.xhtml.viwer.html.mhtml.editor.databinding.ActivityCreateHtmlBinding
import org.sufficientlysecure.htmltextview.HtmlResImageGetter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class CreateHtmlActivity : AppCompatActivity() {
    lateinit var binding:ActivityCreateHtmlBinding

    lateinit var file:File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCreateHtmlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addBtn.setOnClickListener{
            binding.txt.setHtml(
                binding.editTextHtml.text.toString(),
                HtmlResImageGetter(this)
            );
        }

        binding.opensymbolBtn.setOnClickListener {
            binding.editTextHtml.text!!.insert(binding.editTextHtml.selectionStart, binding.opensymbolBtn.text)
        }
        binding.closesymbolBtn.setOnClickListener {
            binding.editTextHtml.text!!.insert(binding.editTextHtml.selectionStart, binding.closesymbolBtn.text)
        }
        binding.slashBtn.setOnClickListener {
            binding.editTextHtml.text!!.insert(binding.editTextHtml.selectionStart, binding.slashBtn.text)
        }

        binding.saveBtn.setOnClickListener {
            val alert = Dialog(this)
            alert.setCancelable(false)
            alert.requestWindowFeature(Window.FEATURE_NO_TITLE)
            alert.setContentView(R.layout.dialog_rename)
            alert.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alert.getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            val fileName = alert.findViewById<EditText>(R.id.fileNameTV)
            val cancelBtn = alert.findViewById<TextView>(R.id.cancelBtn)
            val okBtn = alert.findViewById<TextView>(R.id.okBtn)
            val icon_dialog = alert.findViewById<AppCompatImageView>(R.id.icon_dialog)
            val title_dialog = alert.findViewById<AppCompatTextView>(R.id.title_dialog)

            icon_dialog.setImageDrawable(getDrawable(R.drawable.saveicon))
            title_dialog.text="Save Html File"
//
            //  fileName.setText(newString)
            okBtn.setOnClickListener {
                createHtmlFile(this,fileName.text.toString()+".html",binding.editTextHtml.text.toString())
                alert.dismiss()

            }
            cancelBtn.setOnClickListener {
                alert.dismiss()
            }
            alert.show()


        }

        binding.backBtn.setOnClickListener{
            finish()
        }


    }
    fun createHtmlFile(context: Context?, sFileName: String, sBody: String?) {
        try {
            val root = File(Environment.getExternalStorageDirectory(), "Created HTML Files")
            if (!root.exists()) {
                root.mkdirs()
            }
            file = File(root, sFileName)
            val writer = FileWriter(file)
            writer.append(sBody)
            writer.flush()
            writer.close()
            MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null,
                MediaScannerConnection.OnScanCompletedListener { path, uri ->
                    Log.i("ExternalStorage", "Scanned $path:")
                    Log.i("ExternalStorage", "-> uri=$uri")
                })
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


//    fun createHTML(): String {
//        var format =
//            "<html>" +
//                    "<head>HTML</head>" +
//                    "<body>" +
//                    "Hello World" +
//                    "</body>" +
//                    "</html>";
//        return format
//    }
}