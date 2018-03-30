import io.kotlintest.Spec
import io.kotlintest.specs.ExpectSpec
import io.kotlintest.shouldBe
import io.kotlintest.matchers.exactly
import io.kotlintest.matchers.singleElement

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
        context("processString") {
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
                markovChain1.selectNextWord("test") shouldBe "other"
            }
        }
    }
}