package com.google.firebase.codelab.kit.model.Login_Home_help_pages

import android.app.Activity
import android.content.Intent
//import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
//import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
//import android.widget.ImageView

//import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.codelab.kit.model.Firebase_Storage_Database.ImagesActivity
import com.google.firebase.codelab.kit.model.Firebase_Storage_Database.galleryUpload
import com.google.firebase.codelab.kit.model.Image_Classification.ImageClassifier
import com.google.firebase.codelab.kit.model.Mini_Game.memoryGameActivity
import com.google.firebase.codelab.kit.model.Miscellaneous.BaseActivity
import com.google.firebase.codelab.kit.model.R
import com.google.firebase.codelab.kit.model.Miscellaneous.StillImageActivity
import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import kotlinx.android.synthetic.main.activity_home_page.helpBut
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

open class HomePage : BaseActivity() {

  private var currentPhotoFile: File? = null
  var selectedImageUri: Uri? = null
  var classifier: ImageClassifier? = null
  private  var uploadButton: ImageButton? =null
  var mWelcome: TextView? = null
  private var mAuth: FirebaseAuth? = null



  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home_page)

    val settingsBut = findViewById<View>(R.id.settingsBut) as ImageButton
    val historyBut = findViewById<View>(R.id.histBut) as ImageButton
    val helpBut = findViewById<View>(R.id.helpBut) as ImageButton
    val camBut = findViewById<View>(R.id.cameraBut) as ImageButton
    val minigameBut = findViewById<View>(R.id.mingameBut) as ImageButton
    val logOutBut = findViewById<View>(R.id.logoutBut) as ImageButton
    mWelcome = findViewById(R.id.Welcome)

    uploadButton = findViewById<View>(R.id.uploadBut)as ImageButton

    logOutBut.setOnClickListener {
      val start = Intent(this, LoginActivity::class.java)
      startActivity(start)
    }

    settingsBut.setOnClickListener { }

    historyBut.setOnClickListener {
      val start = Intent(this@HomePage, ImagesActivity::class.java)
      startActivity(start)
    }

    helpBut.setOnClickListener {
      val start = Intent(this@HomePage, HelpPage::class.java)
      startActivity(start)
    }

    minigameBut.setOnClickListener {
      val start = Intent(this, memoryGameActivity::class.java)
      startActivity(start)
    }

    camBut.setOnClickListener { takePhoto() }

    uploadButton!!.setOnClickListener{
      chooseFromLibrary()
    }

    mAuth = FirebaseAuth.getInstance()
    val user = mAuth!!.getCurrentUser()
    var email = user?.email
    val SplitArray = email?.split("@")
    val subemail = SplitArray!![0]
    mWelcome!!.setText("Welcome, " + subemail.toString())
  }

  private fun chooseFromLibrary() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"

    // Only accept JPEG or PNG format.
    val mimeTypes = arrayOf("image/jpeg", "image/png")
    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

    startActivityForResult(intent,
      REQUEST_PHOTO_LIBRARY
    )
  }

  /** Create a file to pass to camera app */
  @Throws(IOException::class)
  private fun createImageFile(): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir = cacheDir
    return createTempFile(
      "JPEG_${timeStamp}_", /* prefix */
      ".jpg", /* suffix */
      storageDir /* directory */
    ).apply {
      // Save a file: path for use with ACTION_VIEW intents.
      currentPhotoFile = this
    }
  }

  open fun takePhoto() {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
      // Ensure that there's a camera activity to handle the intent.
      takePictureIntent.resolveActivity(packageManager)?.also {
        // Create the File where the photo should go.
        val photoFile: File? = try {
          createImageFile()
        } catch (e: IOException) {
          // Error occurred while creating the File.
          Log.e(TAG, "Unable to save image to run classification.", e)
          null
        }
        // Continue only if the File was successfully created.
        photoFile?.also {
          val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "com.google.firebase.codelab.kit.model.fileprovider",
            it
          )
          takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
          startActivityForResult(takePictureIntent,
            REQUEST_IMAGE_CAPTURE
          )
        }
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode != Activity.RESULT_OK) return

    when (requestCode) {
      // Make use of FirebaseVisionImage.fromFilePath to take into account
      // Exif Orientation of the image files.
      REQUEST_IMAGE_CAPTURE -> {
        selectedImageUri = Uri.fromFile(currentPhotoFile)
        FirebaseVisionImage.fromFilePath(this, selectedImageUri!!).also {
          StillImageActivity().classifyImage(it.bitmap)

          val start = Intent(this@HomePage, StillImageActivity::class.java)
          start.putExtra("imageUri", selectedImageUri)
          start.setData(selectedImageUri)
          startActivity(start)
        }
      }
      REQUEST_PHOTO_LIBRARY -> {
        val selectedImageUri = data?.data ?: return
        FirebaseVisionImage.fromFilePath(this, selectedImageUri).also {
          StillImageActivity().classifyImage(it.bitmap)

          val startgallery = Intent(this, galleryUpload::class.java)
          startgallery.putExtra("imageUri", selectedImageUri)
          startgallery.setData(selectedImageUri)
          startActivity(startgallery)
        }
      }
    }
  }






  companion object {
    /** Tag for the [Log].  */
    private const val TAG = "StillImageActivity"

    /** Request code for starting photo capture activity  */
    private const val REQUEST_IMAGE_CAPTURE = 1

    /** Request code for starting photo library activity  */
    private const val REQUEST_PHOTO_LIBRARY = 2
  }
}

