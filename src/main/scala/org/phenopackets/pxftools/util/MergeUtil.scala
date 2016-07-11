package org.phenopackets.pxftools.util

import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.io.RdfGenerator
import org.phenopackets.api.io.RdfReader

import com.hp.hpl.jena.rdf.model.ModelFactory

object MergeUtil {

  private val DummyPacketID = "http://phenopackets.org/mergingpacket"

  def mergePhenoPackets(a: PhenoPacket, b: PhenoPacket, more: PhenoPacket*): PhenoPacket = mergePhenoPackets(more :+ a :+ b)

  def mergePhenoPackets(packets: Iterable[PhenoPacket]): PhenoPacket = {
    val model = ModelFactory.createDefaultModel()
    packets
      .map(packet => RdfGenerator.toRdf(packetWithUpdatedID(packet, DummyPacketID), null))
      .foreach(model.add)
    val newPacket = RdfReader.readModel(model, DummyPacketID)
    packetWithUpdatedID(newPacket, null)
  }

  private def packetWithUpdatedID(packet: PhenoPacket, id: String): PhenoPacket =
    PhenoPacket.newBuilder(packet).id(id).build()

}