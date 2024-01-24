package html.xhtml.viwer.html.mhtml.editor.Activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import br.tiagohm.codeview.CodeView
import br.tiagohm.codeview.Language
import br.tiagohm.codeview.Theme
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import html.xhtml.viwer.html.mhtml.editor.R
import html.xhtml.viwer.html.mhtml.editor.databinding.ActivityViewCodeBinding
import java.io.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ViewCodeActivity : AppCompatActivity(), CodeView.OnHighlightListener {
    lateinit var binding:ActivityViewCodeBinding

    lateinit var filetext: String
    var renamePdf = ""
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityViewCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //   CodeProcessor.init(this);
        val bundle = intent.extras
        val message = bundle!!.getString("file")
        val file= File(message)
        filetext=getTextFromFile(file).toString()
        binding.codeView.setOnHighlightListener(this)
            .setOnHighlightListener(this)
            .setTheme(Theme.ARDUINO_LIGHT)
            .setCode(filetext)
            .setLanguage(Language.HTML)
            .setWrapLine(true)
            .setFontSize(14f)
            .setZoomEnabled(true)
            .setShowLineNumber(true)
            .setStartLineNumber(1)
            .apply()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.pdfConvertBtn.setOnClickListener {
            renameAlert()
        }
    }
    fun getTextFromFile(file: File): String? {
        var textFromFile: String? = null
        if (file.exists()) {
            val text = StringBuilder()
            try {
                val bufferReader = BufferedReader(FileReader(file))
                var lines = bufferReader.readLine()
                while (lines != null) {
                    text.append(lines)
                    text.append('\n')
                    lines = bufferReader.readLine()
                }
                bufferReader.close()
                textFromFile = text.toString()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
        }
        return textFromFile
    }

    override fun onStartCodeHighlight() {

    }

    override fun onFinishCodeHighlight() {

    }

    override fun onLanguageDetected(language: Language?, relevance: Int) {

    }

    override fun onFontSizeChanged(sizeInPx: Int) {

    }

    override fun onLineClicked(lineNumber: Int, content: String?) {

    }
    fun createPdf(context: Context, text: String, mDestFile: String) {
        val doc = Document()
        var outputfile: File?
        try {
            outputfile = File(mDestFile)
            val fOut = FileOutputStream(outputfile)
            PdfWriter.getInstance(doc, fOut)
            //open the document
            doc.open()
            val p1 = Paragraph(filetext)
            p1.alignment = Paragraph.ALIGN_JUSTIFIED
            doc.add(p1)
            val snackbar = Snackbar.make(
                findViewById(R.id.mylayout),
                "Converted",
                Snackbar.LENGTH_LONG
            )
            snackbar.show()
        } catch (de: DocumentException) {
            Log.e("PDFCreator", "DocumentException:$de")
        } catch (e: IOException) {
            Log.e("PDFCreator", "ioException:$e")
        } finally {
            doc.close()
            Log.e("PDFCreator", "ioException:$")
        }

    }
    fun renameAlert() {
        try {
            val pathh = createDir(this, "/PDF Files")

            runOnUiThread {
                val alert = Dialog(this)
                alert.setCancelable(false)
                alert.requestWindowFeature(Window.FEATURE_NO_TITLE)
                alert.setContentView(R.layout.dialog_rename)
                alert.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                alert.getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                val fileName = alert.findViewById<EditText>(R.id.fileNameTV)
                val cancelBtn = alert.findViewById<TextView>(R.id.cancelBtn)
                val okBtn = alert.findViewById<TextView>(R.id.okBtn)
//
                // fileName.setText(newString)
                okBtn.setOnClickListener {
                    executor.execute {
                        renamePdf = fileName.text.toString()
                        val filee = File("$pathh/$renamePdf.pdf")
                        val builder = null
                        createPdf(this, filetext, filee.absolutePath)
                        // createPdf(this, builder.toString(), filee.absolutePath)

                    }
                    handler.post {
                        alert.dismiss()

                    }
                }
                cancelBtn.setOnClickListener {
                    alert.dismiss()
                }
                alert.show()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun createDir(context: Context, dirName: String?): String? {
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
}