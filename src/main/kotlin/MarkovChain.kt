import java.time.Instant
import java.util.Random
import kotlin.math.max

fun String.tokenizeKeepingDelimiters(vararg delimiters: Char): List<String> {
    var lookbehind = "((?<=[" + delimiters.joinToString("") + "]))"
    var lookahead = "((?=[" + delimiters.joinToString("") + "]))"

    val regex = Regex("$lookbehind|$lookahead")

    return this.split(regex).filter { s -> s.isNotEmpty() }
}

class MarkovChain(private val seed: Long = Instant.now().epochSecond) {
    private class WordDistribution(seed: Long) {
        private val doubleGenerator = Random(seed)

        private var totalCount = 0
        private var wordCounts = HashMap<String, Int>()
        private var wordProbabilities = HashMap<String, Double>()

        internal fun update(word: String) {
            totalCount += 1
            wordCounts[word] = wordCounts.getOrPut(word, { 0 }) + 1

            updateAllProbabilities()
        }

        private fun updateAllProbabilities() {
            for (word in wordCounts) {
                wordProbabilities[word.key] = word.value / totalCount.toDouble()
            }
        }

        internal fun probability(word: String): Double {
            return wordProbabilities[word] ?: 0.0
        }

        internal fun selectWord(): String {
            val randomValue = doubleGenerator.nextDouble()
            var cdf = 0.0

            for (word in wordProbabilities) {
                cdf += word.value

                if (randomValue <= cdf) {
                    return word.key
                }
            }

            throw IllegalStateException("Failed to select word from distribution")
        }
    }

    enum class ParagraphMarkers(val marker: String) {
        BEGINNING("^"),
    }

    private val wordDelimiters = charArrayOf(' ', '&')
    private val punctuation = charArrayOf('.', ',', '!', '?', '\'', '"', '(', ')', '-', ';')

    private val sentenceTerminators = setOf('.', '!', '?')
    private val noLeadingSpace = charArrayOf('.', ',', '!', '?', '-', ';')

    private var distribution = HashMap<Pair<String, String>, WordDistribution>()

    private fun invalidCharacters(word: String): Set<Char> {
        val regex = Regex("[^A-z\\d" + punctuation.joinToString("") + "]")
        return regex.findAll(word).map { m -> m.value[0] }.toSet()
    }

    private fun wordValid(word: String): Boolean {
       return invalidCharacters(word).isEmpty()
    }

    fun processString(phrase: String): Set<Char> {
        var unrecognizedCharacters = emptySet<Char>()
        val words = phrase.split(*wordDelimiters).map { w -> w.tokenizeKeepingDelimiters(*punctuation) }.flatten()

        var firstWord = ParagraphMarkers.BEGINNING.marker
        var secondWord = ParagraphMarkers.BEGINNING.marker
        for (targetWord in words) {
            if (!wordValid(targetWord)) {
                unrecognizedCharacters = unrecognizedCharacters.union(invalidCharacters(targetWord))
            } else if (wordValid(firstWord) && wordValid(secondWord)) {
                distribution.getOrPut(Pair(firstWord, secondWord), { WordDistribution(seed) }).update(targetWord)
            }

            if (targetWord.first() in sentenceTerminators) {
                firstWord = ParagraphMarkers.BEGINNING.marker
                secondWord = ParagraphMarkers.BEGINNING.marker
            } else {
                firstWord = secondWord
                secondWord = targetWord
            }
        }

        return unrecognizedCharacters
    }

    fun followProbability(first: String, second: String, target: String): Double {
        return distribution[Pair(first, second)]?.probability(target) ?: 0.0
    }

    fun selectNextWord(first: String, second: String): String {
        return distribution[Pair(first, second)]?.selectWord() ?: ""
    }

    fun generateStory(minimumWords: Int, maximumAttempts: Int = 1): String {
        var wordCount = 0
        var attemptCount = 1
        var firstWord = ParagraphMarkers.BEGINNING.marker
        var secondWord = ParagraphMarkers.BEGINNING.marker
        var currentWord = ""
        var story = ""

        while (wordCount < minimumWords || currentWord.first() !in sentenceTerminators) {
            currentWord = selectNextWord(firstWord, secondWord)
            if (currentWord.isBlank()) {
                if (attemptCount < maximumAttempts) {
                    println("Attempt #$attemptCount failed...")
                    println("No word found to follow `$firstWord` and `$secondWord`.")

                    wordCount = 0
                    firstWord = ParagraphMarkers.BEGINNING.marker
                    secondWord = ParagraphMarkers.BEGINNING.marker
                    story = ""

                    ++attemptCount
                    continue
                } else {
                    throw RuntimeException("Encountered distribution dead-end (no following words). More training data is required.")
                }
            }

            story += if (story.isBlank() || currentWord[0] in noLeadingSpace) "" else " "
            story += currentWord
            ++wordCount

            if (currentWord.first() in sentenceTerminators) {
                firstWord = ParagraphMarkers.BEGINNING.marker
                secondWord = ParagraphMarkers.BEGINNING.marker
            } else {
                firstWord = secondWord
                secondWord = currentWord
            }
        }

        return story
    }
}