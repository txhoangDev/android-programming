package edu.cs371m.peck

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.setPadding
import kotlin.math.max
import kotlin.random.Random

class Words(private val sentenceTV: TextView,
            private val frame: FrameLayout,
            private val random: Random) {
    private val neutralBgColor = Color.rgb(0xCD, 0xCD, 0xCD)
    private val outOfOrderColor = Color.rgb(200, 0, 0)
    private val textViewHeight by lazy {
        val textView = createTextView("Doesn't matter", 0)
        textView.measure(0, 0)
        textView.measuredHeight
    }

    private fun findTVWidth(textView: TextView): Int {
        textView.measure(0, 0)
        return textView.measuredWidth
    }

    private fun createTextView(text: String, index: Int): TextView {
        val lparams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tv = TextView(frame.context)
        tv.layoutParams = lparams
        tv.text = text
        tv.tag = index.toString()
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, sentenceTV.textSize)
        tv.setPadding(20)
        tv.setBackgroundColor(neutralBgColor)
        return tv
    }

    private fun outOfOrderPick(view: View) {
        val colorToWarn : Animator = ValueAnimator
            .ofObject(ArgbEvaluator(), neutralBgColor, outOfOrderColor)
            .apply{duration = 200}
            .apply{addUpdateListener { animator -> view.setBackgroundColor(animator.animatedValue as Int) }}
        val colorFromWarn = ValueAnimator
            .ofObject(ArgbEvaluator(), outOfOrderColor, neutralBgColor)
            .apply{duration = 350}
            .apply{addUpdateListener { animator -> view.setBackgroundColor(animator.animatedValue as Int) }}
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            colorToWarn,
            colorFromWarn
        )
        animatorSet.start()
    }

    fun playRound(numWords: Int, wordsDone: () -> Unit) {
        frame.removeAllViews()
        sentenceTV.setTextColor(Color.BLACK) // Reset color

        var words = listOf<String>()
        // Attempt to pick words until successful
        while (true) {
            val start = random.nextInt(0, PrideAndPrejudice.length)
            try {
                words = PickWords.pick(PrideAndPrejudice, start, numWords)
                break
            } catch (ex: Exception) {
                Log.d("Words.kt", "Pick failed at $start, retrying...")
            }
        }

        sentenceTV.text = words.joinToString(separator = " ")

        // Enforce sentenceTV overflow check
        sentenceTV.post {
            val layout = sentenceTV.layout ?: return@post
            // Check if the layout height exceeds the available height in the TextView
            if (layout.height > (sentenceTV.height - sentenceTV.paddingTop - sentenceTV.paddingBottom)) {
                sentenceTV.text = "numWords is too long"
                sentenceTV.setTextColor(Color.RED)
            } else {
                frame.post {
                    val totalRows = frame.height / textViewHeight

                    if (numWords > totalRows) {
                        val errorTv = createTextView("numWords exceeds available space", 0)
                        frame.addView(errorTv)
                        return@post
                    }

                    // Create unique shuffled row indices for each word to prevent vertical overlap
                    val availableRows = (0 until totalRows).shuffled(random).take(numWords)

                    var nextIndex = 0
                    words.forEachIndexed { currentIndex, word ->
                        val tv = createTextView(word, currentIndex)

                        val tvWidth = findTVWidth(tv)
                        val maxX = max(0, frame.width - tvWidth)
                        val x = random.nextInt(maxX + 1)

                        val row = availableRows[currentIndex]
                        val y = row * textViewHeight

                        val params = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.leftMargin = x
                        params.topMargin = y
                        tv.layoutParams = params

                        tv.setOnClickListener {
                            if (currentIndex == nextIndex) {
                                nextIndex++
                                tv.setBackgroundColor(Color.GREEN)
                                tv.isClickable = false
                                if (nextIndex == words.size) {
                                    wordsDone()
                                }
                            } else {
                                outOfOrderPick(tv)
                            }
                        }
                        frame.addView(tv)
                    }
                }
            }
        }
    }
}
