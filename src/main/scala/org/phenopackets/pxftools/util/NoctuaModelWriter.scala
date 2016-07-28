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
import org.phenopackets.api.model.environment.Environment
import org.phenopackets.api.model.evidence.Evidence
import org.phenopackets.api.model.evidence.Publication
import org.phenopackets.api.model.ontology.ClassInstance
import org.phenopackets.api.util.ContextUtil
import org.phenopackets.pxftools.util.NoctuaModelVocabulary._
import org.phenoscape.scowl._
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat
import org.semanticweb.owlapi.io.StringDocumentTarget
import org.semanticweb.owlapi.model.AddOntologyAnnotation
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLOntology

import com.github.jsonldjava.core.Context
import com.typesafe.scalalogging.LazyLogging

object NoctuaModelWriter extends LazyLogging {

  def fromPhenoPacket(packet: PhenoPacket): OWLOntology = {
    val manager = OWLManager.createOWLOntologyManager()
    val context = ContextUtil.getJSONLDContext(packet)
    val packetIRI = createPacketIRI(packet, context)
    val ont = manager.createOntology(translatePacket(packet, context).asJava, packetIRI)
    Option(packet.getTitle).map(t => new AddOntologyAnnotation(ont, Annotation(DCTitle, t))).foreach(manager.applyChange)
    ont
  }

  def render(packet: PhenoPacket): String = {
    val ont = fromPhenoPacket(packet)
    val target = new StringDocumentTarget()
    ont.getOWLOntologyManager.saveOntology(ont, new ManchesterSyntaxDocumentFormat(), target)
    target.toString
  }

  def translatePacket(packet: PhenoPacket, context: Context): Set[OWLAxiom] = {
    var axioms = Set.empty[OWLAxiom]
    var entityToIndividual = Map.empty[String, OWLNamedIndividual]
    for {
      disease <- packet.getDiseases.asScala
      diseaseID = disease.getId
      (diseaseInd, diseaseAxioms) = translateDisease(disease, context)
    } {
      entityToIndividual += diseaseID -> diseaseInd
      axioms ++= diseaseAxioms
    }
    axioms ++= packet.getPhenotypeAssociations.asScala.flatMap(translatePhenotypeAssociation(_, entityToIndividual, context))
    axioms
  }

  def translateDisease(disease: Disease, context: Context): (OWLNamedIndividual, Set[OWLAxiom]) = {
    val diseaseIRI = iri(disease.getId, context)
    var axioms = Set.empty[OWLAxiom]
    val diseaseIndividual = Individual(newUUIDIRI())
    axioms += Declaration(diseaseIndividual)
    axioms += diseaseIndividual Type Class(diseaseIRI)
    axioms ++= Option(disease.getLabel).map(diseaseIRI Annotation (RDFSLabel, _))
    axioms ++= translateClassInstance(disease, diseaseIndividual, context)
    (diseaseIndividual, axioms)

  }

  def translatePhenotypeAssociation(association: PhenotypeAssociation, entityToIndividual: Map[String, OWLNamedIndividual], context: Context): Set[OWLAxiom] = {
    var axioms = Set.empty[OWLAxiom]
    val entity = entityToIndividual.getOrElse(association.getEntityId, Individual(iri(association.getEntityId, context)))
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
    for {
      contributor <- Option(association.getContributorId)
    } {
      // In the future, contributor should be an IRI
      axioms += associationAxiom Annotation (DCContributor, contributor)
    }
    for {
      date <- Option(association.getDate)
    } {
      axioms += associationAxiom Annotation (DCDate, date)
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
    axioms += pubIndividual Type org.phenopackets.pxftools.util.NoctuaModelVocabulary.Publication
    axioms ++= Option(publication.getTitle).map(pubIndividual Annotation (DCTitle, _))
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
      if (!expanded.startsWith("http")) logger.warn(s"No HTTP URI found for identifier: $id")
      expanded
    } else {
      logger.warn(s"No URI found for identifer: $id")
      s"urn:local:$expanded"
    }
    IRI.create(expandedID)
  }

  private def createPacketIRI(packet: PhenoPacket, context: Context): IRI = Option(packet.getId)
    .map(id => iri(id, context))
    .getOrElse(newUUIDIRI())

  private def newUUIDIRI(): IRI = IRI.create(s"http://model.geneontology.org/${UUID.randomUUID.toString}")

}