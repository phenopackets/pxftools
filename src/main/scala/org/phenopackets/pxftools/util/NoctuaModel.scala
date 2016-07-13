package org.phenopackets.pxftools.util

import java.util.UUID

import scala.collection.JavaConverters._

import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.model.association.PhenotypeAssociation
import org.phenopackets.api.model.condition.Condition
import org.phenopackets.api.model.condition.ConditionFrequency
import org.phenopackets.api.model.condition.ConditionSeverity
import org.phenopackets.api.model.condition.Phenotype
import org.phenopackets.api.model.condition.TemporalRegion
import org.phenopackets.api.model.entity.Disease
import org.phenopackets.api.model.entity.Entity
import org.phenopackets.api.model.environment.Environment
import org.phenopackets.api.model.evidence.Evidence
import org.phenopackets.api.model.evidence.Publication
import org.phenopackets.api.model.ontology.ClassInstance
import org.phenopackets.api.util.ContextUtil
import org.phenoscape.scowl._
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget

import com.github.jsonldjava.core.Context
import com.hp.hpl.jena.vocabulary.DCTerms
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat
import org.semanticweb.owlapi.io.StringDocumentTarget

object NoctuaModel {

  private val factory = OWLManager.getOWLDataFactory
  private val DCTitle = AnnotationProperty(DCTerms.title.getURI)
  private val DCDescription = AnnotationProperty(DCTerms.description.getURI)
  private val RDFSComment = factory.getRDFSComment
  private val RDFSLabel = factory.getRDFSLabel
  private val HasPart = ObjectProperty("http://purl.obolibrary.org/obo/BFO_0000051")
  private val ConditionToFrequency = ObjectProperty("http://example.org/condition_to_frequency") //FIXME
  private val ConditionToSeverity = ObjectProperty("http://example.org/condition_to_severity") //FIXME
  private val TemporalRegionToStart = DataProperty("http://example.org/temporal_region_start_at") //FIXME
  private val TemporalRegionToEnd = DataProperty("http://example.org/temporal_region_end_at") //FIXME
  private val ExistenceStartsDuring = ObjectProperty("http://purl.obolibrary.org/obo/RO_0002488")
  private val ExistenceEndsDuring = ObjectProperty("http://purl.obolibrary.org/obo/RO_0002492")
  private val AxiomHasEvidence = AnnotationProperty("http://purl.obolibrary.org/obo/RO_0002612")
  private val HasSupportingReference = ObjectProperty("http://purl.obolibrary.org/obo/SEPIO_0000124")
  private val Publication = Class("http://purl.obolibrary.org/obo/IAO_0000311")

  def fromPhenoPacket(packet: PhenoPacket): OWLOntology = {
    val manager = OWLManager.createOWLOntologyManager()
    val context = ContextUtil.getJSONLDContext(packet)
    val packetIRI = createPacketIRI(packet, context)
    manager.createOntology(translatePacket(packet, context).asJava, packetIRI)
  }

  def render(packet: PhenoPacket): String = {
    val ont = fromPhenoPacket(packet)
    val target = new StringDocumentTarget()
    ont.getOWLOntologyManager.saveOntology(ont, new ManchesterSyntaxDocumentFormat(), target)
    target.toString
  }

  def translatePacket(packet: PhenoPacket, context: Context): Set[OWLAxiom] = {
    var axioms = Set.empty[OWLAxiom]
    val packetIRI = createPacketIRI(packet, context)
    axioms ++= Option(packet.getTitle).map(packetIRI Annotation (DCTitle, _))
    axioms ++= packet.getDiseases.asScala.flatMap(translateDisease(_, context))
    axioms ++= packet.getPhenotypeAssociations.asScala.flatMap(translatePhenotypeAssociation(_, context))
    axioms
  }

  def translateDisease(disease: Disease, context: Context): Set[OWLAxiom] = {
    entityIRI(disease, context).map { diseaseIRI =>
      var axioms = Set.empty[OWLAxiom]
      val diseaseIndividual = Individual(diseaseIRI) //punning
      axioms += Declaration(diseaseIndividual)
      axioms += diseaseIndividual Type Class(diseaseIRI)
      axioms ++= Option(disease.getLabel).map(diseaseIRI Annotation (RDFSLabel, _))
      axioms ++= translateClassInstance(disease, diseaseIndividual, context)
      axioms
    }.getOrElse(Set.empty)
  }

  def translatePhenotypeAssociation(association: PhenotypeAssociation, context: Context): Set[OWLAxiom] = {
    var axioms = Set.empty[OWLAxiom]
    val entity = Individual(iri(association.getEntityId, context))
    val (phenotype, phenotypeAxioms) = translatePhenotype(association.getPhenotype, context)
    axioms ++= phenotypeAxioms
    val associationAxiom = entity Fact (HasPart, phenotype)
    axioms += associationAxiom
    for {
      (evidenceInd, evidenceAxioms) <- association.getEvidence.asScala.map(translateEvidence(_, context))
    } {
      axioms += associationAxiom Annotation (AxiomHasEvidence, evidenceInd)
      axioms ++= evidenceAxioms
    }
    axioms
  }

  def translatePhenotype(phenotype: Phenotype, context: Context): (OWLNamedIndividual, Set[OWLAxiom]) = {
    var axioms = Set.empty[OWLAxiom]
    val phenoIndividual = Individual(newUUIDIRI())
    axioms += Declaration(phenoIndividual)
    axioms ++= translateCondition(phenotype, phenoIndividual, context)
    phenotype.getMeasurements.asScala.map(_ => ???) //TODO
    (phenoIndividual, axioms)
  }

  def translateCondition(condition: Condition, individual: OWLNamedIndividual, context: Context): Set[OWLAxiom] = {
    var axioms = Set.empty[OWLAxiom]
    axioms ++= translateClassInstance(condition, individual, context)
    Option(condition.getEnvironment).map(_ => ???) //TODO
    for {
      frequency <- Option(condition.getFrequency)
      (freqIndividual, freqAxioms) = translateConditionFrequency(frequency, context)
    } {
      axioms += individual Fact (ConditionToFrequency, freqIndividual)
      axioms ++= freqAxioms
    }
    Option(condition.getHasLocation).map(_ => ???) //TODO
    for {
      severity <- Option(condition.getSeverity)
      (sevIndividual, sevAxioms) = translateConditionSeverity(severity, context)
    } {
      axioms += individual Fact (ConditionToSeverity, sevIndividual)
      axioms ++= sevAxioms
    }
    for {
      finish <- Option(condition.getTimeOfFinishing)
      (finishIndividual, finishAxioms) = translateTemporalRegion(finish, context)
    } {
      axioms += individual Fact (ExistenceEndsDuring, finishIndividual)
      axioms ++= finishAxioms
    }
    for {
      onset <- Option(condition.getTimeOfOnset)
      (onsetIndividual, onsetAxioms) = translateTemporalRegion(onset, context)
    } {
      axioms += individual Fact (ExistenceStartsDuring, onsetIndividual)
      axioms ++= onsetAxioms
    }
    axioms
  }

  def translateEnvironment(environment: Environment, context: Context): (OWLNamedIndividual, Set[OWLAxiom]) = ???

  def translateConditionFrequency(frequency: ConditionFrequency, context: Context): (OWLNamedIndividual, Set[OWLAxiom]) = {
    var axioms = Set.empty[OWLAxiom]
    val frequencyIndividual = Individual(newUUIDIRI())
    axioms += Declaration(frequencyIndividual)
    axioms ++= translateClassInstance(frequency, frequencyIndividual, context)
    (frequencyIndividual, axioms)
  }

  def translateConditionSeverity(severity: ConditionSeverity, context: Context): (OWLNamedIndividual, Set[OWLAxiom]) = {
    var axioms = Set.empty[OWLAxiom]
    val severityIndividual = Individual(newUUIDIRI())
    axioms += Declaration(severityIndividual)
    axioms ++= translateClassInstance(severity, severityIndividual, context)
    (severityIndividual, axioms)
  }

  def translateTemporalRegion(region: TemporalRegion, context: Context): (OWLNamedIndividual, Set[OWLAxiom]) = {
    val regionIndividual = Individual(newUUIDIRI())
    var axioms = Set.empty[OWLAxiom]
    axioms += Declaration(regionIndividual)
    axioms ++= Option(region.getStartTime).map(regionIndividual Fact (TemporalRegionToStart, _))
    axioms ++= Option(region.getEndTime).map(regionIndividual Fact (TemporalRegionToEnd, _))
    axioms ++= translateClassInstance(region, regionIndividual, context)
    (regionIndividual, axioms)
  }

  def translateEvidence(evidence: Evidence, context: Context): (OWLNamedIndividual, Set[OWLAxiom]) = {
    val evidenceIndividual = Individual(newUUIDIRI())
    var axioms = Set.empty[OWLAxiom]
    axioms += Declaration(evidenceIndividual)
    evidence.getSupportingEntities.asScala.map(_ => ???) //TODO
    for {
      (pubIndividual, pubAxioms) <- evidence.getSupportingPublications.asScala.map(translatePublication(_, context))
    } {
      axioms += evidenceIndividual Fact (HasSupportingReference, pubIndividual)
      axioms ++= pubAxioms
    }
    axioms ++= translateClassInstance(evidence, evidenceIndividual, context)
    (evidenceIndividual, axioms)
  }

  def translatePublication(publication: Publication, context: Context): (OWLNamedIndividual, Set[OWLAxiom]) = {
    var axioms = Set.empty[OWLAxiom]
    val pubIndividual = Individual(iri(publication.getId, context))
    axioms += Declaration(pubIndividual)
    axioms += pubIndividual Type Publication
    axioms ++= Option(publication.getTitle).map(pubIndividual Annotation (DCTitle, _)).toSet
    (pubIndividual, axioms)
  }

  def translateClassInstance(instance: ClassInstance, individual: OWLNamedIndividual, context: Context): Set[OWLAxiom] = {
    var axioms = Set.empty[OWLAxiom]
    axioms ++= Option(instance.getDescription).map(individual Annotation (DCDescription, _))
    for {
      ontClass <- instance.getTypes.asScala
      id <- Option(ontClass.getId)
      ontType = Class(iri(id, context))
    } {
      axioms += individual Type ontType
      axioms ++= Option(ontClass.getLabel).map(ontType Annotation (RDFSLabel, _))
    }
    for {
      ontClass <- instance.getNegatedTypes.asScala
      id <- Option(ontClass.getId)
      ontType = Class(iri(id, context))
    } {
      axioms += individual Type (not(ontType))
      axioms ++= Option(ontClass.getLabel).map(ontType Annotation (RDFSLabel, _))
    }
    axioms
  }

  private def iri(id: String, context: Context): IRI = {
    val expanded = ContextUtil.expandIdentifierAsValue(id, context)
    val expandedID = if (expanded.contains(":")) {
      expanded
    } else {
      s"urn:local:$expanded"
    }
    IRI.create(expandedID)
  }

  private def entityIRI(entity: Entity, context: Context): Option[IRI] = Option(entity.getId).map(iri(_, context))

  private def createPacketIRI(packet: PhenoPacket, context: Context): IRI = Option(packet.getId)
    .map(id => iri(id, context))
    .getOrElse(newUUIDIRI())

  private def newUUIDIRI(): IRI = IRI.create(s"urn:uuid:${UUID.randomUUID.toString}")

}