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

@RunWith(classOf[JUnitRunner])
class MergeUtilTest extends Specification with ScalaCheck {

  "MergeUtil" should {
    "mergePhenoPackets" in {

      val packet1 = PhenoPacketGenerator.genPhenopacket.sample.get
      val packet2 = PhenoPacketGenerator.genPhenopacket.sample.get

      val mergedPacket = MergeUtil.mergePhenoPackets(packet1, packet2)

      mergedPacket.getTitle must beEqualTo(packet2.getTitle)
    }

  }

}