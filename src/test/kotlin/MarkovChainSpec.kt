import io.kotlintest.matchers.beGreaterThan
import io.kotlintest.specs.ExpectSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.matchers.exactly
import io.kotlintest.matchers.haveLength
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
                markovChain.processString("test phrase")

                markovChain.followProbability("test", "phrase") shouldBe exactly(1.0)

                markovChain.processString("test other phrase")

                markovChain.followProbability("test", "phrase") shouldBe exactly(0.5)
                markovChain.followProbability("test", "other") shouldBe exactly(0.5)
                markovChain.followProbability("other", "phrase") shouldBe exactly(1.0)
            }

            expect ("sets follow probability using start of sentence marker") {
                val markovChain = MarkovChain()
                markovChain.processString("test")

                markovChain.followProbability(MarkovChain.ParagraphMarkers.BEGINNING.marker, "test") shouldBe exactly(1.0)
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

                markovChain.followProbability("hello", ",") shouldBe exactly(1.0)
                markovChain.followProbability(",", "how") shouldBe exactly(1.0)
                markovChain.followProbability("you", "?") shouldBe exactly(1.0)
                markovChain.followProbability("?", "'") shouldBe exactly(1.0)
                markovChain.followProbability("'", "i") shouldBe exactly(0.5)
                markovChain.followProbability("great", "'") shouldBe exactly(1.0)
                markovChain.followProbability("'", "!") shouldBe exactly(0.5)
                markovChain.followProbability("!", "\"") shouldBe exactly(1.0)
                markovChain.followProbability("\"", "that") shouldBe exactly(0.5)
                markovChain.followProbability("hear", "\"") shouldBe exactly(1.0)
                markovChain.followProbability("\"", ".") shouldBe exactly(0.5)
            }

            expect("selects next word randomly") {
                val markovChain1 = MarkovChain(1).initWithStrings(listOf("test phrase", "test other phrase"))
                val markovChain2 = MarkovChain(7455214303240106663).initWithStrings(listOf("test phrase", "test other phrase"))

                markovChain1.selectNextWord("test") shouldBe "phrase"
                markovChain2.selectNextWord("test") shouldBe "other"
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
                val markovChain = MarkovChain().initWithString("test.")

                shouldThrow<RuntimeException> {
                    markovChain.generateStory(2)
                }
            }
        }
    }
}