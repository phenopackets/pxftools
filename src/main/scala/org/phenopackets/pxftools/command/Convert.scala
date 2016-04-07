package org.phenopackets.pxftools.command

import org.backuity.clist._

object Convert extends Command(description = "Read in a PXF file and output in the specified format.") with Common with SingleInput {

  override def run(): Unit =
    writePhenoPacket(readPhenoPacket(determineInput), determineOutput, outputWriter)

}