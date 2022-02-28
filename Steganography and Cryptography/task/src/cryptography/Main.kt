package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.ImageConsumer
import java.io.File
import java.nio.Buffer
import javax.imageio.ImageIO
import kotlin.system.exitProcess

var inpFile: String =""
var outFile: String =""



fun main() {

    choNado()

}

fun choNado() {
    print("Task (hide, show, exit):\n>")
    val str = readln()
    when(str) {
        "exit" -> {
            println("Bye!")
            exitProcess(0)
        }
        "show" -> {
            println("Obtaining message from image.")
            choNado()
        }
        "hide" -> {
          hide()
            choNado()
        }
        else -> {
            println("Wrong task: $str")
            choNado()
        }
    }

}

fun hide() {
    print("Input image file:\n>")
    inpFile = readln()
    print("Output image file:\n>")
    outFile = readln()
    val inpImage = File(inpFile)
    try {
        var inpBuffer  = ImageIO.read(inpImage)
        for (i in 0 until inpBuffer.width){
            for (j in 0 until inpBuffer.height) {
                val color = Color(inpBuffer.getRGB(i,j))
                val changeColor = Color(color.red or 1, color.green or 1,
                color.blue or 1).rgb
                inpBuffer.setRGB(i,j,changeColor)
            }
        }
        ImageIO.write(inpBuffer, "png", File(outFile))
    }catch (e:Exception) {
        println("Can't read input file!")
        return
    }
    println("Input Image: $inpFile")
    println("Output Image: $outFile")
    println("Image $outFile is saved.")
    choNado()

}

/*

 */