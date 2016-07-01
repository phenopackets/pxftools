package org.phenopackets.pxftools.util

import java.util.UUID

import scala.collection.JavaConverters._
import scala.collection.mutable

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.rdf.model.Statement
import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.io.RDFReader
import org.phenopackets.api.util.ContextUtil

import com.github.jsonldjava.core.Context
import com.github.tototoshi.csv.CSVReader

import org.phenopackets.pxftools.util.PhenoPacketVocabulary._
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.DC

object HPOAnnotations {

  def importFromTable(table: CSVReader): PhenoPacket = {
    val packetURI = s"urn:uuid:${UUID.randomUUID.toString}"
    val packet = ResourceFactory.createResource(packetURI)
    val triples = table.iteratorWithHeaders.flatMap(rowToTriples(_, packet)).toSeq
    val model = ModelFactory.createDefaultModel()
    model.add(triples.asJava)
    model.listStatements().asScala.foreach(println)
    RDFReader.readModel(model, packetURI)
  }

  private def rowToTriples(row: Map[String, String], packet: Resource): Set[Statement] = {
    val statements = mutable.Set.empty[Statement]
    row.get("Disease ID").filter(_.nonEmpty).foreach { diseaseID =>
      val disease = ResourceFactory.createResource(ContextUtil.expandIdentifierAsValue(diseaseID.trim, HPOContext))
      statements += ResourceFactory.createStatement(packet, Diseases, disease)
      row.get("Disease Name").filter(_.nonEmpty).foreach { diseaseLabel =>
        statements += ResourceFactory.createStatement(disease, RDFS.label, ResourceFactory.createTypedLiteral(diseaseLabel.trim))
      }
      row.get("Phenotype ID").filter(_.nonEmpty).foreach { phenotypeID =>
        // will we ever want to add values from other fields even if there is no phenotype class ID?
        val phenotypeType = ResourceFactory.createResource(ContextUtil.expandIdentifierAsValue(phenotypeID.trim, HPOContext))
        val association = ResourceFactory.createResource()
        statements += ResourceFactory.createStatement(packet, PhenotypeProfile, association)
        statements += ResourceFactory.createStatement(association, Entity, disease)
        val phenotype = ResourceFactory.createResource()
        statements += ResourceFactory.createStatement(association, Phenotype, phenotype)
        statements += ResourceFactory.createStatement(phenotype, RDF.`type`, phenotypeType)
        row.get("Phenotype Name").filter(_.nonEmpty).foreach { phenotypeLabel =>
          statements += ResourceFactory.createStatement(phenotypeType, RDFS.label, ResourceFactory.createTypedLiteral(phenotypeLabel.trim))
        }
        row.get("Description").filter(_.nonEmpty).foreach { description =>
          statements += ResourceFactory.createStatement(phenotype, Description, ResourceFactory.createTypedLiteral(description.trim))
        }
        if (row.get("Evidence ID").nonEmpty || row.get("Pub").nonEmpty) {
          val evidence = ResourceFactory.createResource()
          statements += ResourceFactory.createStatement(association, Evidence, evidence)
          row.get("Evidence ID").filter(_.nonEmpty).foreach { evidenceID =>
            val evidenceType = ResourceFactory.createResource(evidenceID.trim) //FIXME
            statements += ResourceFactory.createStatement(evidence, RDF.`type`, evidenceType)
            row.get("Evidence Name").filter(_.nonEmpty).foreach { evidenceName =>
              statements += ResourceFactory.createStatement(evidenceType, RDFS.label, ResourceFactory.createTypedLiteral(evidenceName.trim))
            }
          }
          row.get("Pub").filter(_.nonEmpty).foreach { pubID =>
            val pub = ResourceFactory.createResource(ContextUtil.expandIdentifierAsValue(pubID.trim, HPOContext))
            statements += ResourceFactory.createStatement(evidence, Source, pub)
          }
        }
      }
    }
    statements.toSet
  }

  private val HPOContext: Context = new Context().parse(Map[String, Object](
    "obo" -> "http://purl.obolibrary.org/obo/",
    "HP" -> "obo:HP_",
    "OMIM" -> "obo:OMIM_").asJava)

}