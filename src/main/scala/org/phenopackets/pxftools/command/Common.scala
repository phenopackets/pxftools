package org.phenopackets.pxftools.command

import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter

import org.apache.commons.io.IOUtils
import org.backuity.clist._
import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.io.JsonGenerator
import org.phenopackets.api.io.JsonReader
import org.phenopackets.api.io.YamlGenerator
import org.phenopackets.api.io.YamlReader

trait Common extends Command {

  type PhenoPacketWriter = PhenoPacket => String

  def run(): Unit

  var out = opt[String](description = "Output file. Omit to write to standard out.", default = "")

  var format = opt[String](description = "Output format. Set the output format to one of:\nyaml\njson", default = "yaml")

  def outputWriter: PhenoPacketWriter = format match {
    case "yaml" => YamlGenerator.render _
    case "json" => JsonGenerator.render _
    case _      => throw new ParsingException("Invalid output format.")
  }

  def determineOutput: OutputStream = out match {
    case "" => System.out
    case _  => new FileOutputStream(new File(out))
  }

  def readPhenoPacket(inputStream: InputStream): PhenoPacket = {
    // This is more complicated than it ought to be so that we can reuse 
    // the inputStream to try multiple parsers
    val baos = new ByteArrayOutputStream()
    IOUtils.copy(inputStream, baos)
    val bytes = baos.toByteArray()
    inputStream.close()
    try {
      val bais = new ByteArrayInputStream(bytes);
      val packet = JsonReader.readInputStream(bais)
      bais.close()
      packet
    } catch {
      case ioe: IOException => {
        val bais = new ByteArrayInputStream(bytes);
        val packet = YamlReader.readInputStream(bais)
        bais.close()
        packet
      }
    }
  }

  def writePhenoPacket(packet: PhenoPacket, outputStream: OutputStream, writer: PhenoPacketWriter): Unit = {
    val streamWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"))
    streamWriter.write(writer(packet))
    streamWriter.close()
  }

}