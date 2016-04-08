package org.phenopackets.pxftools.util

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.phenopackets.api.io.YamlGenerator
import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.io.YamlReader
import java.nio.file.Paths
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class MergeUtilTest extends Specification {

  "MergeUtil" should {
    "mergePhenoPackets" in {
      //val packet1 = YamlReader.readFile(Paths.get("src/test/resources/person-environment-association-test.yaml").toFile)
      //val packet2 = YamlReader.readFile(Paths.get("src/test/resources/person-variant-phenotype-example1.yaml").toFile)

      val packetTitle1 = "packet1 title"
      val packetTitle2 = "packet2 title"

      val packet1 = {
        val builder = new PhenoPacket.Builder()
        builder.title(packetTitle1)
        builder.build()
      }

      val packet2 = {
        val builder = new PhenoPacket.Builder()
        builder.title(packetTitle2)
        builder.build()
      }

      val mergedPacket = MergeUtil.mergePhenoPackets(packet1, packet2)

      mergedPacket.getTitle must beEqualTo(packetTitle2)
    }
  }

}