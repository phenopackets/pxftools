package org.phenopackets.pxftools.util

import org.specs2.mutable.Specification
import org.phenopackets.api.io.YamlGenerator
import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.io.YamlReader
import java.nio.file.Paths
import scala.io.Source

import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
import org.specs2.ScalaCheck
import org.phenopackets.pxftools.PhenoPacketGenerator
import org.phenopackets.pxftools.PhenoPackeCollectiontMatchers._
import scala.collection.JavaConversions._
import org.phenopackets.api.model.association.DiseaseOccurrenceAssociation
import java.util.{ List => JList }

@RunWith(classOf[JUnitRunner])
class MergeUtilTest extends Specification with ScalaCheck {

  "MergeUtil" should {
    "mergePhenoPackets" in {

      val packet1 = PhenoPacketGenerator.genPhenopacket.sample.get
      val packet2 = PhenoPacketGenerator.genPhenopacket.sample.get

      val mergedPacket = MergeUtil.mergePhenoPackets(packet1, packet2)

      mergedPacket.getTitle must beEqualTo(packet2.getTitle)
      mergedPacket.getDiseaseOccurrenceAssociations.toList must beTheSameDiseaseOccurrenceAssociationList(packet1.getDiseaseOccurrenceAssociations.toList ::: packet2.getDiseaseOccurrenceAssociations.toList)
      mergedPacket.getDiseases.toList must beTheSameDiseaseList(packet1.getDiseases.toList ::: packet2.getDiseases.toList)
      mergedPacket.getEnvironmentAssociations.toList must beTheSameEnvironmentAssociationList(packet1.getEnvironmentAssociations.toList ::: packet2.getEnvironmentAssociations.toList)
      mergedPacket.getOrganisms.toList must beTheSameOrganismList(packet1.getOrganisms.toList ::: packet2.getOrganisms.toList)
      mergedPacket.getPersons.toList must beTheSamePersonList(packet1.getPersons.toList ::: packet2.getPersons.toList)
      mergedPacket.getPhenotypeAssociations.toList must beTheSamePhenotypeAssociationList(packet1.getPhenotypeAssociations.toList ::: packet2.getPhenotypeAssociations.toList)
      mergedPacket.getVariantAssociations.toList must beTheSameVariantAssociationList(packet1.getVariantAssociations.toList ::: packet2.getVariantAssociations.toList)
      mergedPacket.getVariants.toList must beTheSameVariantList(packet1.getVariants.toList ::: packet2.getVariants.toList)
    }

  }

}