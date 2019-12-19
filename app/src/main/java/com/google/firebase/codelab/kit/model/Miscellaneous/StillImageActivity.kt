/**
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.codelab.kit.model.Miscellaneous

//import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.provider.MediaStore
//import android.provider.MediaStore
//import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
//import androidx.core.content.FileProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.codelab.kit.model.Firebase_Storage_Database.ImagesActivity
import com.google.firebase.codelab.kit.model.Image_Classification.ImageClassifier
import com.google.firebase.codelab.kit.model.Login_Home_help_pages.HomePage
import com.google.firebase.codelab.kit.model.R
import com.google.firebase.codelab.kit.model.RecyclerViewImage.Upload
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import kotlinx.android.synthetic.main.activity_still_image.translation_text
//import java.io.File
import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.Date

open class StillImageActivity : HomePage() {

  //private var currentPhotoFile: File? = null
  open var imagePreview: ImageView? = null
  open var textView: TextView? = null

  //private var currentImageIndex = 0

  private var mProgressBar: ProgressBar? = null
  //private var mEditTextFileName: EditText? = null
  private var mButtonUpload: Button? = null
  private var mTextViewShowUploads: TextView? = null
  private var mButtonTranslate: Button? = null

  private var mStorageRef: StorageReference? = null
  private var mDatabaseRef: DatabaseReference? = null
  private var mUploadTask: StorageTask<*>? = null
  //var captureImage: Button? = null
  //var mButtonLibrary : Button? = null
  private var mAuth: FirebaseAuth? = null

  var vietText: TextView? = null
  var mVietShow : TextView? = null

  private var translate: Translate? = null

  var imageUri :Uri? = null



  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_image_view)


    imagePreview = findViewById(R.id.image_view)
    textView = findViewById(R.id.result_text)
//    captureImage = findViewById<View>(R.id.capture_image) as Button
   // mButtonLibrary = findViewById<View>(R.id.button_choose_image) as Button
    mProgressBar = findViewById<View>(R.id.progress_bar) as ProgressBar
    // mEditTextFileName = findViewById<View>(R.id.edit_text_file_name) as EditText
    mButtonUpload = findViewById<View>(R.id.button_upload) as Button
    mTextViewShowUploads = findViewById<View>(R.id.text_view_show_uploads) as TextView
    vietText = findViewById<View>(R.id.translation_text) as TextView
    mButtonTranslate = findViewById<View>(R.id.transButton) as Button
    mVietShow = findViewById(R.id.vietShow)

    mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
    mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")


    // Setup image classifier.
    try {
      classifier = ImageClassifier(this)
    } catch (e: FirebaseMLException) {
      textView?.text = getString(R.string.fail_to_initialize_img_classifier)
    }

  /*  captureImage!!.setOnClickListener {
      mVietShow?.setVisibility(View.INVISIBLE)
      translation_text.text = ""
    } */

    /*mButtonLibrary!!.setOnClickListener {
      mVietShow?.setVisibility(View.INVISIBLE)
      translation_text.text = ""
      chooseFromLibrary()
    } */

    mButtonUpload!!.setOnClickListener {
      if (mUploadTask != null && mUploadTask!!.isInProgress) { //to check if button already pressed, pressing multiple times wont make it upload twice
        Toast.makeText(this, "Upload in Progress", Toast.LENGTH_SHORT).show()
      } else {
        uploadFile()
      }
    }

    mTextViewShowUploads!!.setOnClickListener { openImagesActivity() }

    mButtonTranslate!!.setOnClickListener {
      if (checkInternetConnection()) {

        //If there is internet connection, get translate service and start translation:
        mVietShow?.setVisibility(View.VISIBLE)
        getTranslateService()
        translate()

      } else {

        //If not, display "no connection" warning:
        translation_text!!.text = resources.getString(R.string.no_connection)
      }
    }

    imageUri = intent.data
    FirebaseVisionImage.fromFilePath(this, imageUri!!).also {classifyImage(it.bitmap)}
  }

  override fun onDestroy() {
    classifier?.close()
    super.onDestroy()
  }

  private fun getFileExtension(uri: Uri): String? {
    val cR = contentResolver
    val mime = MimeTypeMap.getSingleton()
    return mime.getExtensionFromMimeType(cR.getType(uri))
  }

  private fun uploadFile() {
    var engString = "English: "
    var vietString = "Tiếng Việt: "

    val splitArray = textView?.text?.split(",")
    val substring = splitArray!![0]
    var string = substring.replace("Label:", "")
    engString += string

    val vString = vietText?.text.toString()
    vietString += vString
    if (imageUri != null) { //if mImageUri is not empty
      val fileReference = mStorageRef!!.child(
        System.currentTimeMillis().toString() + "."
          + getFileExtension(imageUri!!)
      )
      mUploadTask = fileReference.putFile(imageUri!!)
        .addOnSuccessListener {
          val handler = Handler()
          handler.postDelayed({ mProgressBar!!.progress = 0 }, 500)
          fileReference.downloadUrl.addOnSuccessListener { uri ->
            mAuth = FirebaseAuth.getInstance();
            val user = mAuth!!.getCurrentUser()
            val userID = user!!.getUid()
            val upload =
              Upload(
                engString?.trim { it <= ' ' },
                vietString?.trim() { it <= ' ' },
                uri.toString()
              )
            val uploadID = mDatabaseRef!!.push().key
            mDatabaseRef!!.child(userID!!).child(uploadID!!).setValue(upload)
            Toast.makeText(this, "Upload successfully", Toast.LENGTH_LONG).show()
          }
        }
        .addOnFailureListener { e ->
          Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        .addOnProgressListener { taskSnapshot ->
          val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
          mProgressBar!!.progress = progress.toInt()
        }
    }

  }


  /** Run image classification on the given [Bitmap] */
  fun classifyImage(bitmap: Bitmap) {
    if (classifier == null) {
      StillImageActivity().textView?.text = getString(
        R.string.uninitialized_img_classifier_or_invalid_context
      )
      return

    }

    // Show image on screen.
    val botmap =MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
    imagePreview?.setImageBitmap(botmap)
    imagePreview?.setImageBitmap(bitmap)

    // Classify image.
    classifier?.classifyFrame(bitmap)?.addOnCompleteListener { task ->
      if (task.isSuccessful) {
        textView?.text = task.result
      } else {
        val e = task.exception
        Log.e(TAG, "Error classifying frame", e)
        textView?.text = e?.message
      }
    }
  }



  private fun openImagesActivity() {
    val intent = Intent(this, ImagesActivity::class.java)
    startActivity(intent)
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

  private fun getTranslateService() {

    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)

    try {
      resources.openRawResource(R.raw.credentials).use { `is` ->
        val myCredentials = GoogleCredentials.fromStream(`is`)
        val translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build()
        translate = translateOptions.service
      }
    } catch (ioe: IOException) {
      ioe.printStackTrace()

    }

  }

  private fun translate() {

    val splitArray = textView?.text?.split(",")
    var substring = splitArray!![0]
    var vietstring = substring.replace("Label:", "")

    //Get input text to be translated:
    val originalText: String = vietstring
    val translation = translate!!.translate(originalText, Translate.TranslateOption
      .targetLanguage("vi"), Translate.TranslateOption.model("base"))

    //Translated text and original text are set to TextViews:
    translation_text.text = translation.translatedText

  }

  private fun checkInternetConnection(): Boolean {

    //Check internet connection:
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo

    //Means that we are connected to a network (mobile or wi-fi)
    return activeNetwork?.isConnected == true

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
