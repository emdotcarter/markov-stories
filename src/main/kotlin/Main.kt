import java.io.BufferedReader
import java.io.File

fun main(args: Array<String>) {
    val inputFiles = arrayListOf(
            "data/dracula.txt",
            "data/dracula.txt",
            "data/dracula.txt",
            "data/quixote.txt"
    )

    println("Input files:")
    inputFiles.forEach { s -> println("\t $s") }
    println()

    val markovChain = StringMarkovChain()
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

    val storyLength = 100;
    println("Generating story of approximately $storyLength words...")
    val story = markovChain.generateStory(storyLength)

    println("Generated story:")
    println(story)
}