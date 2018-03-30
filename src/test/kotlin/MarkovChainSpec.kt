import io.kotlintest.matchers.beGreaterThan
import io.kotlintest.specs.ExpectSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.matchers.exactly
import io.kotlintest.matchers.singleElement
import io.kotlintest.matchers.string.haveSameLengthAs
import io.kotlintest.should

fun MarkovChain.initWithString(s: String): MarkovChain {
    this.processString(s)
    return this
}

fun MarkovChain.initWithStrings(ss: List<String>): MarkovChain {
    for (s in ss) {
        this.processString(s)
    }
    return this
}

class MarkovChainSpec : ExpectSpec() {
    init {
        context("processString + followProbability") {
            expect("updates follow probability on each call") {
                val markovChain = MarkovChain()
                markovChain.processString("longer test phrase")

                markovChain.followProbability("longer", "test", "phrase") shouldBe exactly(1.0)

                markovChain.processString("longer test other phrase")

                markovChain.followProbability("longer", "test", "phrase") shouldBe exactly(0.5)
                markovChain.followProbability("longer", "test", "other") shouldBe exactly(0.5)
                markovChain.followProbability("test", "other", "phrase") shouldBe exactly(1.0)
            }

            expect ("sets follow probability using start of sentence marker") {
                val markovChain = MarkovChain()
                markovChain.processString("test this")

                markovChain.followProbability(MarkovChain.ParagraphMarkers.BEGINNING.marker, MarkovChain.ParagraphMarkers.BEGINNING.marker, "test") shouldBe exactly(1.0)
                markovChain.followProbability(MarkovChain.ParagraphMarkers.BEGINNING.marker, "test", "this") shouldBe exactly(1.0)
            }

            expect ("uses start of sentence markers for word following the end of a sentence") {
                val markovChain = MarkovChain()
                markovChain.processString("test this sentence. unique word should be start of sentence.")

                markovChain.followProbability(MarkovChain.ParagraphMarkers.BEGINNING.marker, MarkovChain.ParagraphMarkers.BEGINNING.marker, "test") shouldBe exactly(0.5)
                markovChain.followProbability(MarkovChain.ParagraphMarkers.BEGINNING.marker, MarkovChain.ParagraphMarkers.BEGINNING.marker, "unique") shouldBe exactly(0.5)
            }

            expect ("reports unrecognized characters") {
                val markovChain = MarkovChain()
                val unrecognizedCharacters = markovChain.processString("report this @asdf")

                unrecognizedCharacters shouldBe singleElement('@')
            }
        }

        context("selectNextWord") {
            expect("works with expected punctuation") {
                val markovChain = MarkovChain().initWithString("hello, how are you? 'i am great'! \"that is good to hear\".")

                markovChain.followProbability(MarkovChain.ParagraphMarkers.BEGINNING.marker, "hello", ",") shouldBe exactly(1.0)
                markovChain.followProbability("are", "you", "?") shouldBe exactly(1.0)
                markovChain.followProbability(MarkovChain.ParagraphMarkers.BEGINNING.marker, MarkovChain.ParagraphMarkers.BEGINNING.marker, "'") shouldBe exactly(1.0/3)
                markovChain.followProbability("great", "'", "!") shouldBe exactly(1.0)
                markovChain.followProbability(MarkovChain.ParagraphMarkers.BEGINNING.marker, MarkovChain.ParagraphMarkers.BEGINNING.marker, "\"") shouldBe exactly(1.0/3)
                markovChain.followProbability("hear", "\"", ".") shouldBe exactly(1.0)
            }

            expect("selects next word randomly") {
                val testPhrases = listOf("longer test phrase", "longer test other phrase")
                val markovChain1 = MarkovChain(1).initWithStrings(testPhrases)
                val markovChain2 = MarkovChain(7455214303240106663).initWithStrings(testPhrases)

                markovChain1.selectNextWord("longer", "test") shouldBe "phrase"
                markovChain2.selectNextWord("longer", "test") shouldBe "other"
            }
        }

        context("generateStory") {
            expect("generates a story of at least the required number of words") {
                val sentence = "this is a test, random other non repeated words."
                val markovChain = MarkovChain().initWithString(sentence)

                markovChain.generateStory(2).length should beGreaterThan("this".length)
            }

            expect("only ends at a sentence-terminator") {
                val sentence = "this is a test, random other non repeated words."
                val markovChain = MarkovChain().initWithString(sentence)

                markovChain.generateStory(1) should haveSameLengthAs(sentence)
            }

            expect ("throws an exception if there is not enough data") {
                val markovChain = MarkovChain().initWithString("short test")

                shouldThrow<RuntimeException> {
                    markovChain.generateStory(3)
                }
            }

            expect("can create a story with multiple sentences") {
                val sentence = "hi! this should be printed."
                val markovChain = MarkovChain(123456789012).initWithString(sentence)

                markovChain.generateStory(3) should haveSameLengthAs(sentence)
            }
        }
    }
}