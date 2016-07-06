package org.phenopackets.pxftools.util

import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.io.RDFGenerator
import org.phenopackets.api.io.RDFReader

import com.hp.hpl.jena.rdf.model.ModelFactory

object MergeUtil {

  private val DummyPacketID = "http://phenopackets.org/mergingpacket"

  def mergePhenoPackets(a: PhenoPacket, b: PhenoPacket, more: PhenoPacket*): PhenoPacket = mergePhenoPackets(more :+ a :+ b)

  def mergePhenoPackets(packets: Iterable[PhenoPacket]): PhenoPacket = {
    val model = ModelFactory.createDefaultModel()
    packets
      .map(packet => RDFGenerator.toRDF(packetWithUpdatedID(packet, DummyPacketID), null))
      .foreach(model.add)
    val newPacket = RDFReader.readModel(model, DummyPacketID)
    packetWithUpdatedID(newPacket, null)
  }

  private def packetWithUpdatedID(packet: PhenoPacket, id: String): PhenoPacket = {
    //FIXME phenopacket API needs an easier way to clone with single field update
    new PhenoPacket.Builder()
      .id(id)
      .setContext(packet.getContext)
      .title(packet.getTitle())
      .setDiseaseOccurrenceAssociations(
        packet.getDiseaseOccurrenceAssociations())
      .setDiseases(packet.getDiseases())
      .setEnvironmentAssociations(
        packet.getEnvironmentAssociations())
      .setOrganisms(packet.getOrganisms())
      .setPersons(packet.getPersons())
      .setVariantAssociations(packet.getVariantAssociations())
      .setVariants(packet.getVariants()).build();
  }

}