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

object Merge extends Command(description = "Read in multiple PXF files and output as a single merged PXF file in the specified format.") with Common {

  var files = args[Seq[File]](description = "List of PXF files to merge")

  override def run(): Unit = {

  }

}