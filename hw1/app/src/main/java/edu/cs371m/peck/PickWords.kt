package edu.cs371m.peck

import android.util.Log

class PickWords {
    companion object {
        private val punctSpaceStr = " \t\n._,:;“”?!-"

        private fun findStart(input: String, start: Int): Int {
            val length = input.length
            var i = start

            // skip current word if in middle
            while (i < length && !punctSpaceStr.contains(input[i])) {
                i++
            }

            // skip delims
            while (i < length && punctSpaceStr.contains(input[i])) {
                i++
            }

            return i
        }

        private fun getLastOccurring(words: List<String>, word: String): Int {
            val regex = Regex("""^${Regex.escape(word)}(\((\d+)\))?$""")

            // get last match
            val lastMatch = words.findLast { it.matches(regex) } ?: word

            // find the different values of the match
            val matchResult = regex.find(lastMatch)

            // get the number otherwise default to 0
            return matchResult?.groups?.get(2)?.value?.toInt() ?: 0
        }

        fun pick(input: String, start: Int, numWords: Int): List<String> {
            val words = mutableListOf<String>()
            val length = input.length

            // determine if in middle of word
            var i = start.coerceIn(0, length - 1)
            if (i > 0 && !punctSpaceStr.contains(input[i])) {
                i = findStart(input, start)
            }
            Log.d("PickWords.kt", "Beginning index of first word is $i")

            while (i < length && words.size < numWords) {
                // create word
                val word = StringBuilder(input.substring(i).takeWhile { !punctSpaceStr.contains(it) })

                // ensure adding non-empty word
                if (word.isNotEmpty()) {
                    Log.d("PickWords.kt", "GOT WORD " + word)
                    // handling duplicates
                    if (words.contains(word.toString())) {
                        val occurrences = getLastOccurring(words, word.toString())
                        word.append("(${occurrences + 1})")
                    }
                    words.add(word.toString())
                }

                // go to next word
                i = findStart(input, i)
            }

            return words
        }
    }
}