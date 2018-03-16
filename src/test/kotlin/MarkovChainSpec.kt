import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import kotlin.test.*

object StringMarkovChainSpec: Spek({
    given("a string markov chain") {
        on("processString") {
            val markovChain = StringMarkovChain()
            markovChain.processString("test phrase")
            markovChain.processString("test other phrase")

            it("updates the frequency of words with each phrase") {
                assertEquals(1.0, markovChain.followProbability(StringMarkovChain.ParagraphMarkers.BEGINNING.marker, "test"))
                assertEquals(0.5, markovChain.followProbability("test", "phrase"))
                assertEquals(0.5, markovChain.followProbability("test", "other"))
                assertEquals(1.0, markovChain.followProbability("other", "phrase"))
            }

            it("reports unrecognized characters") {
                val unrecognizedCharacters = markovChain.processString("should report this @asdf")
                assertEquals(setOf('@'), unrecognizedCharacters)
            }
        }

        on("selectNextWord") {
            it("works with punctuation and special characters") {
                val markovChain = StringMarkovChain()
                markovChain.processString("hello, how are you? 'i am great'! \"that is good to hear\".")

                assertEquals(1.0, markovChain.followProbability("hello", ","))
                assertEquals(1.0, markovChain.followProbability(",", "how"))
                assertEquals(1.0, markovChain.followProbability("you", "?"))
                assertEquals(1.0, markovChain.followProbability("?", "'"))
                assertEquals(0.5, markovChain.followProbability("'", "i"))
                assertEquals(1.0, markovChain.followProbability("great", "'"))
                assertEquals(0.5, markovChain.followProbability("'", "!"))
                assertEquals(1.0, markovChain.followProbability("!", "\""))
                assertEquals(0.5, markovChain.followProbability("\"", "that"))
                assertEquals(1.0, markovChain.followProbability("hear", "\""))
                assertEquals(0.5, markovChain.followProbability("\"", "."))
            }
        }
    }

    given("a seeded markov chain") {
        it("returns first following word given appropriate random seeding") {
            val markovChain = StringMarkovChain(1)
            markovChain.processString("test phrase")
            markovChain.processString("test other phrase")

            assertEquals("phrase", markovChain.selectNextWord("test"))
        }

        it("returns second following word given appropriate random seeding") {
            var markovChain = StringMarkovChain(7455214303240106663)
            markovChain.processString("test phrase")
            markovChain.processString("test other phrase")

            assertEquals("other", markovChain.selectNextWord("test"))
        }
    }
})