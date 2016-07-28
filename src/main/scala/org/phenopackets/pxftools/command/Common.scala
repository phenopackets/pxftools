package org.phenopackets.pxftools.command

import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter

import org.apache.commons.io.IOUtils
import org.apache.jena.riot.Lang
import org.backuity.clist._
import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.io.JsonGenerator
import org.phenopackets.api.io.JsonReader
import org.phenopackets.api.io.RdfGenerator
import org.phenopackets.api.io.YamlGenerator
import org.phenopackets.api.io.YamlReader
import org.phenopackets.pxftools.util.HPOAnnotations
import org.phenopackets.pxftools.util.NoctuaModelWriter
import org.phenopackets.pxftools.util.NoctuaDiseasePhenotypeModelReader

trait Common extends Command {

  type PhenoPacketWriter = PhenoPacket => String
  type PhenoPacketReader = InputStream => PhenoPacket

  def run(): Unit

  var out = opt[String](description = "Output file. Omit to write to standard out.", default = "")

  var informat = opt[String](description =
    """Input format. By default both yaml and json will be attempted. Set the input format to one of:
yaml
json
hpo-phenote
noctua-disease-phenotype""", default = "guess")
  var outformat = opt[String](description = "Output format. Set the output format to one of:\nyaml\njson\nturtle\nnoctua", default = "yaml")

  def inputReader: Option[PhenoPacketReader] = informat match {
    case "yaml"                     => Option(YamlReader.readInputStream)
    case "json"                     => Option(JsonReader.readInputStream)
    case "hpo-phenote"              => Option(HPOAnnotations.read)
    case "noctua-disease-phenotype" => Option(NoctuaDiseasePhenotypeModelReader.read)
    case "guess"                    => None
    case _                          => throw new ParsingException("Invalid input format.")
  }

  def outputWriter: PhenoPacketWriter = outformat match {
    case "yaml"   => YamlGenerator.render
    case "json"   => JsonGenerator.render
    case "turtle" => RdfGenerator.render(_, null, Lang.TURTLE) //TODO should we ask for a base?
    case "noctua" => NoctuaModelWriter.render
    case _        => throw new ParsingException("Invalid output format.")
  }

  def determineOutput: OutputStream = out match {
    case "" => System.out
    case _  => new FileOutputStream(new File(out))
  }

  def readPhenoPacket(inputStream: InputStream): PhenoPacket = {
    inputReader.map(_(inputStream)).getOrElse {
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
  }

  def readPhenoPacketFile(file: File): PhenoPacket = {
    try {
      JsonReader.readFile(file)
    } catch {
      case ioe: IOException => {
        YamlReader.readFile(file)
      }
    }
  }

  def writePhenoPacket(packet: PhenoPacket, outputStream: OutputStream, writer: PhenoPacketWriter): Unit = {
    val streamWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"))
    streamWriter.write(writer(packet))
    streamWriter.close()
  }

}