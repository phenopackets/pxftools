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

trait SingleInput extends Command {

  var infile = arg[Option[String]](description = "Input file. Omit to read from standard in.")

  def determineInput: InputStream = infile match {
    case Some("-")      => System.in
    case Some(filepath) => new FileInputStream(new File(filepath))
    case None           => System.in
  }

}