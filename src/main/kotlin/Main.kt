import java.io.BufferedReader
import java.io.File

fun main(args: Array<String>) {
    val inputFiles = arrayListOf(
            "data/dracula.txt",
            "data/dracula.txt",
            "data/ruby_thomas05a.txt"
    )

    println("Input files:")
    inputFiles.forEach { s -> println("\t $s") }
    println()

    val markovChain = MarkovChain()
    inputFiles.forEach { s ->
        println("Processing $s...")
        val bufferedReader: BufferedReader = File(s).bufferedReader()

        bufferedReader.useLines { lines ->
            lines.forEach { l ->
                val unrecognizedCharacters = markovChain.processString(l)
                if (unrecognizedCharacters.isNotEmpty()) {
                    println("UNRECOGNIZED CHARACTERS: ${unrecognizedCharacters.joinToString()}")
                }
            }
        }
    }

    val storyLength = 50
    println("Generating story of approximately $storyLength words...")
    val story = markovChain.generateStory(storyLength, 50)

    println("Generated story:")
    println(story)
}