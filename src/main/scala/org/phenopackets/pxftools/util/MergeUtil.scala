package org.phenopackets.pxftools.util

import scala.collection.JavaConverters._

import org.phenopackets.api.PhenoPacket

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

import scalaz. _ 
import scalaz.Scalaz. _ 

object MergeUtil {

  implicit object PhenoPacketJsonNodeSemigroup extends Semigroup[JsonNode] {

    override def append(a: JsonNode, b: => JsonNode): JsonNode = (a, b) match {
      case (aObj: ObjectNode, bObj: ObjectNode) => {
        val fields = (aObj.fieldNames.asScala ++ bObj.fieldNames.asScala).toSet
        val unionObject = JsonNodeFactory.instance.objectNode()
        for {
          field <- fields
        } {
          val newValue = Option(aObj.get(field)) |+| Option(bObj.get(field))
          newValue.foreach(unionObject.replace(field, _))
        }
        unionObject
      }
      case (aList: ArrayNode, bList: ArrayNode) => {
        val (aHaveID, aNoID) = aList.elements.asScala.partition(_.has("id"))
        val (bHaveID, bNoID) = bList.elements.asScala.partition(_.has("id"))
        val mergedWithSameID = (aHaveID ++ bHaveID).map(obj => Map(obj.get("id").asText -> obj)).fold(Map.empty)(_ |+| _)
        val unionArray = JsonNodeFactory.instance.arrayNode()
        unionArray.addAll(aNoID.toSeq.asJava)
        unionArray.addAll(bNoID.toSeq.asJava)
        unionArray.addAll(mergedWithSameID.values.toSeq.asJava)
        unionArray
      }
      case _ => b
    }

  }

  /**
   * Merge phenopackets via JSON mapping. This may be much less efficient
   * than merging directly with builder API, but takes much less code
   * and should be robust to schema/API changes.
   */
  def mergePhenoPackets(a: PhenoPacket, b: PhenoPacket): PhenoPacket = {
    val mapper = new ObjectMapper()
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    val aJSON: JsonNode = mapper.valueToTree[JsonNode](a)
    val bJSON: JsonNode = mapper.valueToTree[JsonNode](b)
    val mergedJSON = aJSON |+| bJSON
    mapper.treeToValue(mergedJSON, classOf[PhenoPacket])
  }

}
