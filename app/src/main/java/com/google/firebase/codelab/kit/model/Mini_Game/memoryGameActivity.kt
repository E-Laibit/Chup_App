package com.google.firebase.codelab.kit.model.Mini_Game

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

import com.google.firebase.codelab.kit.model.Login_Home_help_pages.HomePage
import com.google.firebase.codelab.kit.model.R.drawable.*
import kotlinx.android.synthetic.main.activity_game.*
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.codelab.kit.model.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import pl.droidsonroids.gif.GifImageView
import java.util.Locale

class memoryGameActivity : HomePage() {

  internal var textView: TextView? = null

  private var mTextViewCountDown: TextView? = null
  private var mCountDownTimer: CountDownTimer? = null
  private var mTimerRunning: Boolean = false
  public var mTimeLeftInMillis = START_TIME_IN_MILLIS
  var gettingReady: FrameLayout? = null
  var memoryGame: RelativeLayout? = null
  var mInstructions :TextView? = null
  var mHowToPlay : RelativeLayout? = null
  var mGameLogo : ImageView? = null
  var mLayout : RelativeLayout? = null
  var mExitBut : Button? = null
  var mRetryBut : Button? = null
  var mHighscoreButton: Button? = null
  var mHighScoreFirstBut : Button? = null
  lateinit var timeLeftFormatted: String

  var mYes : Button? = null
  var mNo : Button? = null
  var gif : GifImageView? = null
  var losegif : GifImageView? = null
  var mResultGame : TextView? = null
  var mCongrats1 : TextView? = null
  var mCongrats2 : TextView? = null
  var mRemain : TextView? = null
  var mGameProgressBar : ProgressBar? = null

  private var mAuth: FirebaseAuth? = null
  private var mDatabaseRef: DatabaseReference? = null

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)

    gettingReady = findViewById(R.id.gettingReady)
    memoryGame = findViewById(R.id.memoryGame)
    mHowToPlay = findViewById(R.id.howtoplay)
    mGameLogo = findViewById(R.id.gameLogo)
    gif = findViewById(R.id.spongegif)
    mLayout = findViewById(R.id.mLayout)
    mResultGame = findViewById(R.id.result_game)
    mCongrats1 = findViewById(R.id.CongratsSign1)
    mCongrats2 = findViewById(R.id.CongratsSign2)
    mRemain = findViewById(R.id.remaining)
    mGameProgressBar = findViewById(R.id.progress_bar)
    losegif = findViewById(R.id.betterluckgif)
    mExitBut = findViewById(R.id.exitButton)
    mRetryBut = findViewById(R.id.retryButton)
    mHighscoreButton = findViewById(R.id.highscoreBut)
    mHighScoreFirstBut = findViewById(R.id.firstHighScore)

    mLayout?.visibility = View.INVISIBLE
    gettingReady?.visibility = View.VISIBLE
    mHowToPlay?.visibility = View.VISIBLE
    mGameLogo?.visibility = View.VISIBLE
    memoryGame?.visibility = View.INVISIBLE
    gif?.visibility = View.INVISIBLE
    losegif?.visibility = View.INVISIBLE
    mCongrats2?.visibility = View.INVISIBLE
    mCongrats1?.visibility = View.INVISIBLE
    mRemain?.visibility = View.INVISIBLE
    mHighscoreButton?.visibility = View.INVISIBLE
    mHighScoreFirstBut?.visibility = View.VISIBLE

    mTextViewCountDown = findViewById(R.id.text_view_countdown)
    mYes = findViewById(R.id.yesButton)
    mNo = findViewById(R.id.noButton)

    mInstructions = findViewById(R.id.intructionsTextView)
    mDatabaseRef = FirebaseDatabase.getInstance().getReference("games")

    mInstructions?.setText(" 1. Press 'Yes!!!' when you are ready to start the game." + "\n 2. " +
      "Take" +
      " note that the timer will start right away." + "\n 3. You are only allowed to flip 2 " +
      "images at once" + "\n 4. You must flip the same image back if incorrect" + "\n    and open " +
      "new sets"
      + "\n 5. Find all the matching pairs and you win the game!")

    val images : MutableList<Int> = mutableListOf(
    cup, cutlery, cutlery_1, fork,  knife_1, spoon,  knife_1, spoon, cup, cutlery, cutlery_1, fork)

    val Buttons = arrayOf(Button1, Button2, Button3, Button4, Button5, Button6, Button7, Button8,
      Button9, Button10, Button11, Button12)

    var clicked = 0 //know how many cards have been clicked
    var turnOver = false //If there are 2 cards being turned over
    var lastClicked = -1 //for matching
    var winningCount = 0

    val cardBack = pinknobackground

    mExitBut?.setOnClickListener {
      val start = Intent(this, HomePage::class.java)
      startActivity(start)
      Toast.makeText(this, "I hope you do better next time", Toast.LENGTH_SHORT).show()
    }

    mRetryBut?.setOnClickListener {
      finish()
      startActivity(intent)
    }

    mYes?.setOnClickListener {

      gettingReady?.visibility = View.INVISIBLE
      mHowToPlay?.visibility = View.INVISIBLE
      mGameLogo?.visibility = View.INVISIBLE
      mHighScoreFirstBut?.visibility = View.INVISIBLE
      if (memoryGame?.visibility == View.INVISIBLE){
        memoryGame?.visibility = View.VISIBLE
        mLayout?.visibility = View.VISIBLE
      }
      startTimer()
      mGameProgressBar!!.progress = 0
    }

    mNo?.setOnClickListener {
      val start = Intent(this, HomePage::class.java)
      startActivity(start)
      Toast.makeText(this, "I hope you're ready next time", Toast.LENGTH_SHORT).show()
    }

    mHighScoreFirstBut?.setOnClickListener {
      val nointent = Intent(this  , HighscoreOutside::class.java)
      startActivity(nointent)
      finish()
    }

    mHighscoreButton?.setOnClickListener {
        val intent = Intent(this  , Highscore::class.java)
        var timeLeftNoS = timeLeftFormatted.replace("s", "")
        var timeSpent = 30 - timeLeftNoS.toInt()
        var timing = timeSpent.toString()
        intent.putExtra("timing", timing)
        startActivity(intent)
        finish()

    }

    images.shuffle()
    for (i in 0..11){
      Buttons[i].setBackgroundResource(cardBack)
      Buttons[i].text = "cardBack"
      Buttons[i].textSize = 0.0F
      Buttons[i].setOnClickListener {

        if (Buttons[i].text == "cardBack" && !turnOver) {

          Buttons[i].setBackgroundResource(images[i])
          Buttons[i].setText(images[i])
          if (clicked == 0) {
            lastClicked = i
          }
          clicked++
        } else if (Buttons[i].text !in "cardBack") {
          Buttons[i].setBackgroundResource(cardBack)
          Buttons[i].text = "cardBack"
          clicked-- //everytime i flip it, minus the one that is clicked from the "clicked" variable
        }

        if (clicked == 2) {
          turnOver = true //if the image is turned, not able to turn others
          // it over
          if (Buttons[i].text == Buttons[lastClicked].text) { //if both images are equal using
            // their text name
            Buttons[i].isClickable = false
            Buttons[lastClicked].isClickable = false
            turnOver = false //they wont be able to turn it back(the correct ones)
            winningCount++
            clicked = 0  //can click new cards
          }
        } else if (clicked == 0) {
          turnOver = false // they will only be able to flip if the "clicked" variable is 0
        }
        if (winningCount == 6){

          var toast = Toast.makeText(this, "Well Done! You have Completed the Game!", Toast
          .LENGTH_LONG)
          val v = toast.view.findViewById<View>(android.R.id.message) as TextView
          if( v != null) v.setGravity(Gravity.CENTER)
          toast.show()


          stopTimer()
          var timeLeftNoS = timeLeftFormatted.replace("s", "")
          var timeSpent = 30 - timeLeftNoS.toInt()
          mResultGame!!.setText(timeSpent.toString() + " s")

          mCongrats1!!.setText("You managed to complete the game! ")
          mCongrats2!!.setText("You Completed within :")
          mGameProgressBar?.visibility = View.INVISIBLE
          mTextViewCountDown?.visibility = View.INVISIBLE
          memoryGame?.visibility = View.INVISIBLE
          mCongrats2?.visibility = View.VISIBLE
          mCongrats1?.visibility = View.VISIBLE
          mHighscoreButton?.visibility = View.VISIBLE
          uploadScore()

          gif?.visibility = View.VISIBLE
        }

      }

    }

    textView = findViewById(R.id.textView)
  }


  private fun uploadScore(){
    mAuth = FirebaseAuth.getInstance()
    val user = mAuth!!.getCurrentUser()
    val userID = user!!.getUid()
    val userString = user.email.toString()
    val SplitArray = userString?.split("@")
    val userEmail = SplitArray!![0]
    val uploadID = mDatabaseRef!!.push().key

    var timeLeftNoS = timeLeftFormatted.replace("s", "")
    var timeSpent = 30 - timeLeftNoS.toInt()

    if (timeLeftFormatted != null){
      val uploadscore = User(timeSpent)

      mDatabaseRef!!.child(userID).push().setValue(uploadscore)
    }
  }

  fun stopTimer(){
    if(mCountDownTimer != null){
      mCountDownTimer!!.cancel()
      mCountDownTimer = null
    }
  }
  private fun resetTimer() {
    mTimeLeftInMillis = START_TIME_IN_MILLIS
    updateCountDownText()
  }

  private fun startTimer() {
    mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
      override fun onTick(millisUntilFinished: Long) {
        mTimeLeftInMillis = millisUntilFinished
        updateCountDownText()
        var progress = (millisUntilFinished / 305.0).toInt()
        mGameProgressBar!!.progress = progress

      }

      override fun onFinish() {
        mTimerRunning = false
        mGameProgressBar!!.setProgress(-1)
        lostGame()
      }
    }.start()
  }

  private fun lostGame(){
     var toast = Toast.makeText(this, "You have ran out of time! Better luck next time!", Toast
    .LENGTH_LONG)
    val v = toast.view.findViewById<View>(android.R.id.message) as TextView
    if( v != null) v.setGravity(Gravity.CENTER)
    toast.show()

    mCongrats1!!.setText("You have failed to complete the game ")
    mCongrats2!!.setText("Time for more practice!")

    mGameProgressBar?.visibility = View.INVISIBLE
    mTextViewCountDown?.visibility = View.INVISIBLE
    memoryGame?.visibility = View.INVISIBLE
    losegif?.visibility = View.VISIBLE
    mCongrats2?.visibility = View.VISIBLE
    mCongrats1?.visibility = View.VISIBLE
    mExitBut?.visibility = View.VISIBLE
    mRetryBut?.visibility = View.VISIBLE
  }

  public fun updateCountDownText() {
    val seconds = (mTimeLeftInMillis / 1000).toInt() % 60

    timeLeftFormatted = String.format(Locale.getDefault(), "%02d" + "s",  seconds)

    mTextViewCountDown!!.text = timeLeftFormatted
  }

  companion object {

    private val START_TIME_IN_MILLIS: Long = 30000
  }
}
