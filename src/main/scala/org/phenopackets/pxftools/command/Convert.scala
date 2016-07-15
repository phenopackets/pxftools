package org.phenopackets.pxftools.command

import org.backuity.clist._
import org.phenopackets.api.PhenoPacket
import java.io.File

object Convert extends Command(description = "Read in a PXF file and output in the specified format.") with Common with SingleInput {

  override def run(): Unit = {
    val packet = readPhenoPacket(determineInput)
    val packetWithTitle = (infile, Option(packet.getTitle)) match {
      case (Some(filename), None) if ((filename != "-")) => {
        PhenoPacket.newBuilder(packet).title(new File(filename).getName).build()
      }
      case _ => packet
    }
    writePhenoPacket(packetWithTitle, determineOutput, outputWriter)
  }

}