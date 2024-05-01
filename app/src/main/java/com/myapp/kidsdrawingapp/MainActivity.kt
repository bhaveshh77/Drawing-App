package com.myapp.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.myapp.kidsdrawingapp.databinding.ActivityMainBinding
import com.myapp.kidsdrawingapp.databinding.BrushDialogBinding
import com.myapp.kidsdrawingapp.databinding.DialogCustomProgressBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
//    private lateinit var drawingView: DrawingView
    private lateinit var binding: ActivityMainBinding
    private lateinit var colorBtn : ImageButton
    private var snackBarShown = false
    private var customDialog: Dialog? = null
    private val getLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {uri : Uri? ->

        binding.drawingImg.setImageURI(uri)
    }

    private val requestPermission : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            run {
                permissions.entries.forEach {
                    val permissionName = it.key
                    val isGranted = it.value

                    if (isGranted) {
                        if (!snackBarShown) {
                            Snackbar.make(binding.mainConstraintLayout, "Permission Granted!!", Snackbar.LENGTH_SHORT).show()
                            snackBarShown = true
                        }

                        pickImage()
                    } else {
                        if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                            if (!snackBarShown) {
                                Snackbar.make(binding.mainConstraintLayout, "Permission Denied!!", Snackbar.LENGTH_SHORT).show()
                                snackBarShown = true
                            }

                        }

                    }

                }

            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.drawingView.setBrushSize(20.toFloat())

        colorBtn = binding.colors[1] as ImageButton

        binding.brushBtn.setOnClickListener {
            showBrushSizeDialog()
        }

        binding.addImage.setOnClickListener {

            requestStoragePermissions()
        }

        binding.undoBtn.setOnClickListener {

            binding.drawingView.onUndoClicked()
        }

        binding.redoBtn.setOnClickListener {

            binding.drawingView.onRedoClicked()
        }

        binding.saveBtn.setOnClickListener {

            showCustomDialog()
            if(isReadStorageAllowed()) {
                lifecycleScope.launch {
                    saveImage(getBitmapFromView(binding.frameDrawing))
                    /* or
                    *  val bitmap = getBitmapFromView(binding.frameDrawing)
                    * saveImage(bitmap)
                    * */
                }
            } else {
                Snackbar.make(binding.mainConstraintLayout, "Please Allow the Permission!!", Snackbar.LENGTH_SHORT).show()
            }

        }

    }

    private fun isReadStorageAllowed() : Boolean {
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {

        if(ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRationaleDialog("Kids Drawing App", "Kids Drawing App " +
                    "needs to Access Your External Storage!!")
        } else {
            requestPermission.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                // Todo Add the writing external storage permission in the array! : Added already!!!
            ))

//            If the permission is needed:
//
//Show a dialog explaining why the app needs access to external storage.
//Otherwise (if permission is not needed or already explained):
//
//Request permission to read external storage.
        }
    }

    fun paintClicked(view : View) {

        if(view !== colorBtn) {
            val image = view as ImageButton
            val colorTag = image.tag as String
            binding.drawingView.setBrushColor(colorTag)

            image.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )

            colorBtn.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )

            colorBtn = view
        }

    }

    private fun showRationaleDialog(
        title : String,
        message : String
    ) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") {dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()


    }

    private fun getBitmapFromView(view: View) : Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        // Create a Canvas to draw the View onto the Bitmap
        val canvas = Canvas(returnedBitmap)

        val viewBg = view.background

        if(viewBg != null) {
            viewBg.draw(canvas)
        }
//        } else {
//            canvas.drawColor(Color.WHITE)
//        }

        view.draw(canvas)

        // Return the Bitmap
        return returnedBitmap

    }

    private suspend fun saveImage(bitmap: Bitmap?) : String {
        var result = ""
        withContext(Dispatchers.IO) {
            if(bitmap != null) {
                try {
                    // Step 1: Convert Bitmap to ByteArray
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    // Step 2: Create a File to Save the Bitmap
                    val f = File(externalCacheDir?.absoluteFile.toString()
                            + File.separator + "KidsDrawingApp_" + System.currentTimeMillis()/100 + ".png")

                    // Step 3: Write the ByteArray to the File
                    val fo = FileOutputStream(f)
//                    Imagine the file as a container, and the FileOutputStream is like a pipe connected to this container.

                    fo.write(bytes.toByteArray())
                    //fo.write(bytes.toByteArray()) is like pouring the contents of the ByteArray (the compressed image) into the container through the pipe.

                    fo.close()
                    //fo.close() is like putting a cap on the pipe, saying, "I'm done pouring things in."


                    // Step 4: Set the File Path to the 'result' variable
                    result = f.absolutePath

                    runOnUiThread {
                        dismissCustomDialog()
                        if(result.isNotEmpty()) {
                            Toast.makeText(this@MainActivity, "Filed Saved Successfully : $result", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: IOException) {
                    // Handle any errors that might occur during the process
                    result = ""
                    e.printStackTrace()
                }
            }
        }

        return result
    }

    private fun showBrushSizeDialog() {


//            View dialogView = LayoutInflater.from(context).inflate(R.layout.quantity_dialog, null); Without viewBinding....!!!!!
        val dialogBinding: BrushDialogBinding =
            BrushDialogBinding.inflate(LayoutInflater.from(applicationContext))

        val customDialog = Dialog(this)

        customDialog.setContentView(dialogBinding.root)

        dialogBinding.smallBrushBtn.setOnClickListener {
            binding.drawingView.setBrushSize(10.toFloat())
            customDialog.dismiss()
        }

        dialogBinding.mediumBrushBtn.setOnClickListener {
            binding.drawingView.setBrushSize(20.toFloat())
            customDialog.dismiss()
        }

        dialogBinding.largeBrushBtn.setOnClickListener {
            binding.drawingView.setBrushSize(30.toFloat())
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun showCustomDialog() {
        customDialog = Dialog(this@MainActivity)

        val customProgressBinding : DialogCustomProgressBinding =
            DialogCustomProgressBinding.inflate(LayoutInflater.from(applicationContext))

        customDialog!!.setContentView(customProgressBinding.root)

        customDialog!!.show()
    }

    private fun dismissCustomDialog() {
        if(customDialog != null) {
            customDialog!!.dismiss()
            customDialog = null
        }
    }

    private fun pickImage() {

        getLauncher.launch("image/*")
    }

//    Something changes!
}