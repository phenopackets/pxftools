package org.phenopackets.pxftools.util

import org.phenoscape.scowl._
import org.semanticweb.owlapi.apibinding.OWLManager

import com.hp.hpl.jena.vocabulary.DC_11

object NoctuaModelVocabulary {

  private val factory = OWLManager.getOWLDataFactory

  val DCTitle = AnnotationProperty(DC_11.title.getURI)
  val DCDescription = AnnotationProperty(DC_11.description.getURI)
  val DCSource = AnnotationProperty(DC_11.source.getURI)
  val DCDate = AnnotationProperty(DC_11.date.getURI)
  val DCContributor = AnnotationProperty(DC_11.contributor.getURI)
  val RDFSComment = factory.getRDFSComment
  val HasPart = ObjectProperty("http://purl.obolibrary.org/obo/BFO_0000051")
  val ConditionToFrequency = ObjectProperty("http://example.org/condition_to_frequency") //FIXME
  val ConditionToSeverity = ObjectProperty("http://example.org/condition_to_severity") //FIXME
  val TemporalRegionToStart = DataProperty("http://example.org/temporal_region_start_at") //FIXME
  val TemporalRegionToEnd = DataProperty("http://example.org/temporal_region_end_at") //FIXME
  val ExistenceStartsDuring = ObjectProperty("http://purl.obolibrary.org/obo/RO_0002488")
  val ExistenceEndsDuring = ObjectProperty("http://purl.obolibrary.org/obo/RO_0002492")
  val AxiomHasEvidence = AnnotationProperty("http://purl.obolibrary.org/obo/RO_0002612")
  val HasSupportingReference = ObjectProperty("http://purl.obolibrary.org/obo/SEPIO_0000124")
  val Publication = Class("http://purl.obolibrary.org/obo/IAO_0000311")

}