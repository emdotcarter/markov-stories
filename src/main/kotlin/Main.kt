import java.io.BufferedReader
import java.io.File

fun main(args: Array<String>) {
    val inputFiles = arrayListOf(
            Pair("data/dracula.txt", 0),
            Pair("data/eula.txt", 30),
            Pair("data/progcpp.txt", 0),
            Pair("data/quixote.txt", 0),
            Pair("data/small_test.txt", 0),
            Pair("data/tale_of_two_cities.txt", 1),
            Pair("data/wizard_of_oz.txt", 0)
    )

    println("Input files:")
    inputFiles.filter { p -> p.second > 0 }.forEach { p -> println("\t ${p.first}: weight = ${p.second}") }
    println()

    val markovChain = MarkovChain()
    var unrecognizedCharacters = emptySet<Char>()
    inputFiles.filter { p -> p.second > 0 }.forEach { p ->
        println("Processing ${p.first}...")

        val bufferedReader: BufferedReader = File(p.first).bufferedReader()
        bufferedReader.useLines { lines ->
            lines.forEach { l ->
                repeat(p.second, {
                    unrecognizedCharacters = unrecognizedCharacters.union(markovChain.processString(l))
                })
            }
        }
    }

    if (unrecognizedCharacters.isNotEmpty()) {
        println("UNRECOGNIZED CHARACTERS: ${unrecognizedCharacters.joinToString(" ")}")
    }

    val storyLength = 1000
    val attempts = 1000
    println("Generating story of approximately $storyLength words...")
    val story = markovChain.generateStory(storyLength, attempts)

    println("Generated story:")
    println(story.replace("([.?!])".toRegex(), "$1|").split("|").sortedBy { s -> s.length }.joinToString("\n"))
}