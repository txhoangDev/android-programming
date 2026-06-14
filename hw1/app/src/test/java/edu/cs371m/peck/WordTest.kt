package edu.cs371m.peck

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(
    RobolectricTestRunner::class
)
class WordTest {

    private fun testWords(start: Int, numWords: Int, expectedList: List<String>) {
        val wordList = PickWords.pick(PrideAndPrejudice, start, numWords)
        //print(wordList)
        assertEquals(expectedList, wordList)
    }

    // Here is a very basic test to get you started.
    @Test
    fun testIndexStart0() {
        testWords(
            0, 8,
            listOf(
                "PRIDE", "AND", "PREJUDICE", "By", "Jane", "Austen", "Chapter", "1"
            )
        )
    }

    // test to get next word if in middle of word
    @Test
    fun testIndexStartInMiddle() {
        testWords(
            1, 8,
            listOf("AND", "PREJUDICE", "By", "Jane", "Austen", "Chapter", "1", "It")
        )
        testWords(
            3, 2,
            listOf("AND", "PREJUDICE")
        )
    }

    // start on whitespace
    @Test
    fun testStartAtWhitespace() {
        val wordList = PickWords.pick("HELLO    WORLD", 5, 1)
        val expectedList = listOf("WORLD")
        assertEquals(expectedList, wordList)
    }

    // start on punctuation
    @Test
    fun testStartAtPunctuation() {
        val wordList = PickWords.pick("good-humoured people", 0, 3)
        val expectedList = listOf("good", "humoured", "people")
        assertEquals(expectedList, wordList)
    }

    // tests duplicates
    @Test
    fun testStringWithDuplicates() {
        val wordList = PickWords.pick("he he he he", 0, 4)
        val expectedList = listOf("he", "he(1)", "he(2)", "he(3)")
        assertEquals(expectedList, wordList)
    }

    // test with multiple weird punctuatiosn
    @Test
    fun testManyPunctuations() {
        val wordList = PickWords.pick("Well...this,is:interesting", 0, 4)
        val expectedList = listOf("Well", "this", "is", "interesting")
        assertEquals(expectedList, wordList)
    }

    // test with duplicates and punctuations
    @Test
    fun testDuplicatesWithPunctuations() {
        val wordList = PickWords.pick("he, he! he? he", 0, 4)
        val expectedList = listOf("he", "he(1)", "he(2)", "he(3)")
        assertEquals(expectedList, wordList)
    }

    // test duplicates, case sensitivity
    @Test
    fun testDuplicatesCaseSensitive() {
        val wordList = PickWords.pick("he He he He", 0, 4)
        val expectedList = listOf("he", "He", "he(1)", "He(1)")
        assertEquals(expectedList, wordList)
    }

    // numWords > words in string
    @Test
    fun testExcessNumWords() {
        val wordList = PickWords.pick("he, he! he? he", 0, 6)
        val expectedList = listOf("he", "he(1)", "he(2)", "he(3)")
        assertEquals(expectedList, wordList)
    }

    // start on punct/space before last word
    @Test
    fun testCharBeforeLast() {
        val wordList = PickWords.pick("one two", 6, 1)
        val expectedList = listOf<String>()
        assertEquals(expectedList, wordList)
    }

    // Only punctuation
    @Test
    fun testOnlyPunctuation() {
        val wordList = PickWords.pick(",,  \n\t", 0, 4)
        val expectedList = listOf<String>()
        assertEquals(expectedList, wordList)
    }

    // start past last word
    @Test
    fun testPastLastWord() {
        val wordList = PickWords.pick("TWO,,  \n\t", 5, 4)
        val expectedList = listOf<String>()
        assertEquals(expectedList, wordList)
    }

}
