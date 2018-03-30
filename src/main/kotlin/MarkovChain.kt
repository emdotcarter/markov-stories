import java.time.Instant
import java.util.Random

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

    private var distribution = HashMap<String, WordDistribution>()

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

        var previousWord = ParagraphMarkers.BEGINNING.marker
        for (w in words) {
            if (!wordValid(w)) {
                unrecognizedCharacters = unrecognizedCharacters.union(invalidCharacters(w))
            } else if (wordValid(previousWord)) {
                distribution.getOrPut(previousWord, { WordDistribution(seed) }).update(w)
            }

            previousWord = w
        }

        return unrecognizedCharacters
    }

    fun followProbability(first: String, second: String): Double {
        return distribution[first]?.probability(second) ?: 0.0
    }

    fun selectNextWord(word: String): String {
        return distribution[word]?.selectWord() ?: ""
    }

    fun generateStory(minimumWords: Int): String {
        var i = 0
        var currentWord = selectNextWord(ParagraphMarkers.BEGINNING.marker)
        var story = currentWord
        while (i < minimumWords || currentWord[0] !in sentenceTerminators) {
            if (currentWord.isBlank()) {
                throw RuntimeException("Encountered distribution dead-end (no following words). More training data is required.")
            }

            currentWord = selectNextWord(currentWord)
            story += if (currentWord[0] in noLeadingSpace) "" else " "
            story += currentWord

            ++i
        }

        return story
    }
}