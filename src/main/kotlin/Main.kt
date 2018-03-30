import java.io.BufferedReader
import java.io.File

fun main(args: Array<String>) {
    val inputFiles = arrayListOf(
            Pair("data/dracula.txt", 3),
            Pair("data/quixote.txt", 2),
            Pair("data/small_test.txt", 0),
            Pair("data/wizard_of_oz.txt", 4)
    )

    println("Input files:")
    inputFiles.filter { p -> p.second > 0 }.forEach { p -> println("\t ${p.first}: weight = ${p.second}") }
    println()

    val markovChain = MarkovChain()
    inputFiles.filter { p -> p.second > 0 }.forEach { p ->
        println("Processing ${p.first}...")

        val bufferedReader: BufferedReader = File(p.first).bufferedReader()
        bufferedReader.useLines { lines ->
            lines.forEach { l ->
                repeat(p.second, {
                    val unrecognizedCharacters = markovChain.processString(l)
                    if (unrecognizedCharacters.isNotEmpty()) {
                        println("UNRECOGNIZED CHARACTERS: ${unrecognizedCharacters.joinToString()}")
                    }
                })
            }
        }
    }

    val storyLength = 100
    val attempts = 1000
    println("Generating story of approximately $storyLength words...")
    val story = markovChain.generateStory(storyLength, attempts)

    println("Generated story:")
    println(story.replace(".", ".\n"))
}