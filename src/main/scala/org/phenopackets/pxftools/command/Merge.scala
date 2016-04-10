package org.phenopackets.pxftools.command

import java.io.File

import org.backuity.clist._
import org.phenopackets.pxftools.util.MergeUtil

object Merge extends Command(description = "Read in multiple PXF files and output as a single merged PXF file in the specified format.") with Common {

  var files = args[Seq[File]](description = "List of PXF files to merge")

  override def run(): Unit = {
    val mergedPhenoPacket = files.map(readPhenoPacketFile).reduce(MergeUtil.mergePhenoPackets)
    writePhenoPacket(mergedPhenoPacket, determineOutput, outputWriter)
  }

}