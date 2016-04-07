package org.phenopackets.pxftools.util

import org.phenopackets.api.PhenoPacket
import scala.collection.JavaConverters._
import scala.collection.mutable.Buffer
import java.util.{ List => JList }

object MergeUtil {

  def mergePhenoPackets(packet1: PhenoPacket, packet2: PhenoPacket): PhenoPacket = {
    val builder = new PhenoPacket.Builder()
    builder.title(packet2.getTitle)
    builder.setOrganisms(
      (nullcheck(packet1.getOrganisms) ++ nullcheck(packet2.getOrganisms)).asJava)
    builder.setPersons(
      (nullcheck(packet1.getPersons) ++ nullcheck(packet2.getPersons)).asJava)
    builder.setPhenotypeAssociations(
      (nullcheck(packet1.getPhenotypeAssociations) ++ nullcheck(packet2.getPhenotypeAssociations)).asJava)
    builder.setDiseases(
      (nullcheck(packet1.getDiseases) ++ nullcheck(packet2.getDiseases)).asJava)
    builder.setDiseaseOccurrenceAssociations(
      (nullcheck(packet1.getDiseaseOccurrenceAssociations) ++ nullcheck(packet2.getDiseaseOccurrenceAssociations)).asJava)
    builder.setEnvironmentAssociations(
      (nullcheck(packet1.getEnvironmentAssociations) ++ nullcheck(packet2.getEnvironmentAssociations)).asJava)
    builder.setVariants(
      (nullcheck(packet1.getVariants) ++ nullcheck(packet2.getVariants)).asJava)
    builder.build()
  }

  private def nullcheck[T](list: JList[T]): Buffer[T] = Option(list).map(_.asScala).getOrElse(Buffer.empty)

}