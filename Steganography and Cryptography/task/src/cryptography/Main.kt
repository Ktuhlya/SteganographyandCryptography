package cryptography

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

// UI commands
const val CMD_HIDE = "hide"
const val CMD_SHOW = "show"
const val CMD_EXIT = "exit"

// UI messages
const val UI_PROMPT = "Task (hide, show, exit):"
const val MSG_NOT_FOUND = "Hidden message not found!"
const val MSG_EXIT = "Bye!"
const val MSG_WRONG_CMD = "Wrong task:"
const val MSG_INPUT_FILENAME = "Input image file:"
const val MSG_OUTPUT_FILENAME = "Output image file:"
const val MSG_IMAGE_TOO_SMALL = "The input image is not large enough to hold this message."
const val MSG_TO_HIDE = "Message to hide:"
const val MSG_SHOW = "Message:"

const val IMG_TYPE = "png"

// 24-bit string representation of bytes 0,0,3 - each byte is 8-bits
const val END_OF_MESSAGE = "000000000000000000000011"

fun main() {
    do {
        println(UI_PROMPT)
        val userInput = readLine()!!.lowercase()
        when (userInput) {
            CMD_HIDE -> cmdHide()
            CMD_SHOW -> cmdShow()
            CMD_EXIT -> println(MSG_EXIT)
            else -> println("$MSG_WRONG_CMD $userInput")
        }
    } while (userInput != CMD_EXIT)
}

/**
 * Displays prompt message and reads user's answer from standard input
 * @return string representing user's typed answer
 */
fun getUserInput(promptMessage: String): String {
    println(promptMessage)
    return readLine()!!
}

/**
 * Converts byte to its unsigned 8-bit string representation
 * @return 8-character string, left padded with zeros if necessary
 */
fun Byte.toBinaryString8(): String {
    return String
        .format("%${Byte.SIZE_BITS}s", this.toString(2))
        .replace(' ', '0')
}

/**
 * Converts a string to a bit-representation, where each bit is an Integer with value `0` or `1`
 * @return List of integers
 */
fun encodeToBits(message: String): List<Int> {
    return message.encodeToByteArray()
        .map { eachByte ->
            eachByte
                .toBinaryString8()
                .map { it.digitToInt() }
        }
        .flatten()
}

/**
 * Reads the bit representation of a string from an Integer list of `0`s & `1`s
 * and converts the entire sequence of bits to one string
 * @return string
 */

fun decodeFromBits(bitList: List<Int>): String {
    return bitList
        .windowed(Byte.SIZE_BITS, Byte.SIZE_BITS)
        {
            it
                .joinToString("")
                .toByte(2)
        }
        .toByteArray()
        .toString(Charsets.UTF_8)
}

/**
 * Checks if the message is too big to fit inside the image
 * @return true, if the image is too small to hold the message
 */
fun isImageTooSmall(image: BufferedImage, message: List<Int>): Boolean {
    return message.size > image.width * image.height
}

/**
 * Inserts the message bits into the least significant bits of color BLUE
 * @return image with the message hidden inside it
 */
fun imageWithMessage(bufferedImage: BufferedImage, message: List<Int>): BufferedImage {
    val imgWidth = bufferedImage.width
    val imgHeight = bufferedImage.height
    var bitCount = 0

    loop@ for (y in 0 until imgHeight) {
        for (x in 0 until imgWidth) {
            if (bitCount == message.size)                    // we wrote all the message bits
                break@loop

            val rgb = bufferedImage.getRGB(x, y)

            if (rgb and 1 != message[bitCount])              // if blue's last bit don't match the bit we need to write
                bufferedImage.setRGB(x, y, rgb xor 1)    // `xor 1` flips the bit value: 1->0 and 0->1

            bitCount++                                       // move to the next bit we need to write
        }
    }
    return bufferedImage
}

/**
 *
 */
fun messageFromImage(bufferedImage: BufferedImage): String {
    val imgWidth = bufferedImage.width
    val imgHeight = bufferedImage.height
    val bitList = mutableListOf<Int>()

    for (y in 0 until imgHeight) {
        for (x in 0 until imgWidth) {
            if (END_OF_MESSAGE in bitList.joinToString(""))
                return decodeFromBits(bitList.dropLast(END_OF_MESSAGE.length))

            bitList.add(bufferedImage.getRGB(x, y) and 1)       // bitwise operation `and 1` returns the last bit
        }
    }

    return MSG_NOT_FOUND
}

/**
 * Hides the message inside the copy of an existing image file
 */
fun cmdHide() {

    val inImgFile = File(getUserInput(MSG_INPUT_FILENAME))
    val outImgFile = File(getUserInput(MSG_OUTPUT_FILENAME))
    val message = encodeToBits(getUserInput(MSG_TO_HIDE)) + END_OF_MESSAGE.map { it.digitToInt() }

    val inputImage: BufferedImage
    try {
        inputImage = ImageIO.read(inImgFile)
    } catch (e: IOException) {
        println("${e.message}")
        return
    }

    if (isImageTooSmall(inputImage, message)) {
        println(MSG_IMAGE_TOO_SMALL)
        return
    }

    try {
        ImageIO.write(
            imageWithMessage(inputImage, message),
            IMG_TYPE,
            outImgFile
        )
        // Change the file path delimiter character from `\` (Windows) to `/` (Linux, macOS), otherwise unit test fails
        println("Message saved in ${outImgFile.toString().replace('\\', '/')} image.")
    } catch (e: IOException) {
        println("${e.message}")
    }
}

/**
 * Reads the hidden message from an image file and displays it
 */
fun cmdShow() {
    val inImgFile = File(getUserInput(MSG_INPUT_FILENAME))
    val inputImage: BufferedImage

    try {
        inputImage = ImageIO.read(inImgFile)
    } catch (e: IOException) {
        println("${e.message}")
        return
    }

    println("$MSG_SHOW\n${messageFromImage(inputImage)}")
}

//////////////////
/*
package cryptography

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.Exception

const val EOM = "003"

fun hideMessage() {
    val (inFile, outFile, msg) = hideData()
    val image: BufferedImage = try {
        val imageFile = File(inFile)
        ImageIO.read(imageFile)
    } catch (e: Exception) {
        println("Can't read input file!")
        return
    }
    val newFile = File(outFile)
    try {
        ImageIO.write(hideInImage(image, msg), "png", newFile)
    } catch (e: Exception) {
        println(e.message)
        return
    }
    println("Message saved in $outFile image.")
}

fun hideData(): Triple<String, String, ByteArray> {
    println("Input image file:")
    val inFile = readLine()!!
    println("Output image file:")
    val outFile = readLine()!!
    println("Message to hide:")
    val msg = readLine()!!.encodeToByteArray() + EOM.map { it.toString().toInt().toByte() }
    return Triple(inFile, outFile, msg)
}

fun hideInImage(image: BufferedImage, msg: ByteArray): BufferedImage {
    if (msg.size * Byte.SIZE_BITS > image.width * image.height) {
        throw Exception("The input image is not large enough to hold this message.")
    }
    val newImage: BufferedImage = image
    var pos = 0
    for (byte in msg) {
        for (bit in bitList(byte)) {
            val col = pos % image.width
            val row = pos / image.width
            val c = image.getRGB(col, row)
            val color = if (bit == 1) c or 1 else (c.inv() or 1).inv()
            newImage.setRGB(col, row, color )
            pos++
        }
    }
    return newImage
}

fun bitList(byte: Byte): List<Int> {
    val bits = emptyList<Int>().toMutableList()
    for (index in 0 until Byte.SIZE_BITS) {
        bits.add(byte.toInt() shr index and 1)
    }
    return bits.reversed()
}

fun showMessage() {
    println("Input image file:")
    val filename = readLine()!!
    val image: BufferedImage = try {
        val imageFile = File(filename)
        ImageIO.read(imageFile)
    } catch (e: Exception) {
        println("Can't read input file!")
        return
    }
    println("Message:")
    println(decodeImage(image))
}

fun decodeImage(image: BufferedImage): String {
    val byteMsg = emptyList<Byte>().toMutableList()
    var pos = 0
    while (!endOfMessage(byteMsg)) {
        val bits = emptyList<Int>().toMutableList()
        for (index in 0 until Byte.SIZE_BITS) {
            val col = pos % image.width
            val row = pos / image.width
            bits.add(image.getRGB(col, row) and 1)
            pos++
        }
        byteMsg.add(bitListToByte(bits))
    }
    return byteMsg.subList(0, byteMsg.size - EOM.length).map { it.toInt().toChar() }.joinToString("")
}

fun bitListToByte(bits: MutableList<Int>): Byte {
    var newByte: Int = 0
    for (bit in bits) {
        newByte = newByte shl 1
        newByte += bit
    }
    return newByte.toByte()
}

fun endOfMessage(msg: MutableList<Byte>): Boolean {
    if (msg.size >= EOM.length) {
        val end = EOM.map { it.toString().toInt().toByte() }
        return msg.reversed().subList(0, end.size) == end.reversed()
    } else {
        return false
    }
}

fun main() {
    while (true) {
        println("\nTask (hide, show, exit):")
        when (val task = readLine()!!) {
            "hide" -> hideMessage()
            "show" -> showMessage()
            "exit" -> break
            else -> println("Wrong task: $task")
        }
    }
    println("Bye!")
}
 */