package org.phenopackets.pxftools.util

import java.io.InputStream

import scala.collection.JavaConverters._

import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.model.association.PhenotypeAssociation
import org.phenopackets.api.model.condition.Phenotype
import org.phenopackets.api.model.entity.Disease
import org.phenopackets.api.model.evidence.Evidence
import org.phenopackets.api.model.evidence.Publication
import org.phenopackets.api.model.ontology.OntologyClass
import org.phenopackets.pxftools.util.NoctuaModelVocabulary._
import org.phenoscape.scowl._
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.AxiomType
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLOntology

import com.google.common.base.Optional
import com.typesafe.scalalogging.LazyLogging

import scalaz._
import scalaz.Scalaz._
import org.phenopackets.api.model.condition.ConditionFrequency
import org.phenopackets.api.model.ontology.ClassInstance
import org.phenopackets.api.model.condition.ConditionSeverity
import org.phenopackets.api.model.condition.TemporalRegion

object NoctuaDiseasePhenotypeModelReader extends LazyLogging {

  def read(stream: InputStream): PhenoPacket = {
    val manager = OWLManager.createOWLOntologyManager()
    val ont = manager.loadOntologyFromOntologyDocument(stream)
    stream.close()
    createPhenoPacket(ont)
  }

  def createPhenoPacket(ont: OWLOntology): PhenoPacket = {
    val builder = PhenoPacket.newBuilder()
    ont.getOntologyID.getOntologyIRI.asScala.foreach(iri => builder.id(iri.toString))
    val associationsAndAnnotations = (for {
      ObjectPropertyAssertion(annotations, HasPart, subj: OWLNamedIndividual, obj: OWLNamedIndividual) <- ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION).asScala
    } yield Map(Association(subj, obj) -> annotations)).reduce(_ |+| _)
    var diseases = Set.empty[Disease]
    for {
      (Association(diseaseInd, phenotypeInd), annotations) <- associationsAndAnnotations
    } {
      val allDiseaseTypes = findOWLTypes(diseaseInd, ont)
      if (allDiseaseTypes.size > 1) {
        logger.warn(s"More than one type found for disease $diseaseInd; keeping only one to use as the entity ID.")
      }
      allDiseaseTypes.headOption.foreach { diseaseClass =>
        val disease = new Disease()
        disease.setId(diseaseClass.getIRI.toString)
        findLabel(diseaseClass, ont).foreach(disease.setLabel)
        findDescription(diseaseInd, ont).foreach(disease.setLabel)
        diseases += disease
        val phenotype = new Phenotype()
        updateAsClassInstance(phenotype, phenotypeInd, ont)
        findFrequency(phenotypeInd, ont).foreach(phenotype.setFrequency)
        findSeverity(phenotypeInd, ont).foreach(phenotype.setSeverity)
        findTimeOfOnset(phenotypeInd, ont).foreach(phenotype.setTimeOfOnset)
        findTimeOfOffset(phenotypeInd, ont).foreach(phenotype.setTimeOfFinishing)
        val associationBuilder = new PhenotypeAssociation.Builder(phenotype)
        associationBuilder.setEntity(disease)
        annotations.foreach {
          case Annotation(AxiomHasEvidence, evidence: IRI) => associationBuilder.addEvidence(toEvidence(Individual(evidence), ont))
          case Annotation(DCContributor, contributor ^^ _) => associationBuilder.setContributorId(contributor)
          case Annotation(DCDate, date ^^ _) => associationBuilder.setDate(date)
          case _ =>
        }
        builder.addPhenotypeAssociation(associationBuilder.build())
      }
    }
    builder.setDiseases(diseases.toSeq.asJava)
    builder.build()
  }

  private def toEvidence(ind: OWLNamedIndividual, ont: OWLOntology): Evidence = {
    val evidence = new Evidence()
    updateAsClassInstance(evidence, ind, ont)
    val publications = for {
      ObjectPropertyAssertion(_, HasSupportingReference, _, reference: IRI) <- ont.getObjectPropertyAssertionAxioms(ind).asScala
    } yield {
      val pubBuilder = new Publication.Builder()
      pubBuilder.setId(reference.toString)
      findLabel(Individual(reference), ont).foreach(pubBuilder.setTitle)
      pubBuilder.build()
    }
    evidence.setSupportingPublications(publications.toSeq.asJava)
    evidence
  }

  private def toOntologyClass(term: OWLClass, ont: OWLOntology): OntologyClass = {
    val classBuilder = new OntologyClass.Builder(term.getIRI.toString)
    findLabel(term, ont).foreach(classBuilder.setLabel)
    classBuilder.build()
  }

  private def findLabel(item: OWLEntity, ont: OWLOntology): Option[String] = (for {
    AnnotationAssertion(_, RDFSLabel, _, label ^^ _) <- ont.getAnnotationAssertionAxioms(item.getIRI).asScala
  } yield label).headOption

  private def findDescription(item: OWLEntity, ont: OWLOntology): Option[String] = (for {
    AnnotationAssertion(_, DCDescription, _, desc ^^ _) <- ont.getAnnotationAssertionAxioms(item.getIRI).asScala
  } yield desc).headOption

  private def findFrequency(phenotypeInd: OWLNamedIndividual, ont: OWLOntology): Option[ConditionFrequency] = (for {
    ObjectPropertyAssertion(_, ConditionToFrequency, _, frequencyInd: OWLNamedIndividual) <- ont.getObjectPropertyAssertionAxioms(phenotypeInd).asScala
  } yield {
    val frequency = new ConditionFrequency()
    updateAsClassInstance(frequency, frequencyInd, ont)
    frequency
  }).headOption

  private def findSeverity(phenotypeInd: OWLNamedIndividual, ont: OWLOntology): Option[ConditionSeverity] = (for {
    ObjectPropertyAssertion(_, ConditionToSeverity, _, severityInd: OWLNamedIndividual) <- ont.getObjectPropertyAssertionAxioms(phenotypeInd).asScala
  } yield {
    val severity = new ConditionSeverity()
    updateAsClassInstance(severity, severityInd, ont)
    severity
  }).headOption

  private def findTimeOfOnset(phenotypeInd: OWLNamedIndividual, ont: OWLOntology): Option[TemporalRegion] = (for {
    ObjectPropertyAssertion(_, ExistenceStartsDuring, _, onsetInd: OWLNamedIndividual) <- ont.getObjectPropertyAssertionAxioms(phenotypeInd).asScala
  } yield {
    toTemporalRegion(onsetInd, ont)
  }).headOption

  private def findTimeOfOffset(phenotypeInd: OWLNamedIndividual, ont: OWLOntology): Option[TemporalRegion] = (for {
    ObjectPropertyAssertion(_, ExistenceEndsDuring, _, offsetInd: OWLNamedIndividual) <- ont.getObjectPropertyAssertionAxioms(phenotypeInd).asScala
  } yield {
    toTemporalRegion(offsetInd, ont)
  }).headOption

  private def toTemporalRegion(regionInd: OWLNamedIndividual, ont: OWLOntology): TemporalRegion = {
    val region = new TemporalRegion()
    updateAsClassInstance(region, regionInd, ont)
    for {
      ObjectPropertyAssertion(_, TemporalRegionToStart, _, start ^^ _) <- ont.getObjectPropertyAssertionAxioms(regionInd).asScala
    } {
      region.setStartTime(start)
    }
    for {
      ObjectPropertyAssertion(_, TemporalRegionToEnd, _, end ^^ _) <- ont.getObjectPropertyAssertionAxioms(regionInd).asScala
    } {
      region.setEndTime(end)
    }
    region

  }

  private def updateAsClassInstance(instance: ClassInstance, ind: OWLNamedIndividual, ont: OWLOntology): Unit = {
    findDescription(ind, ont).foreach(instance.setDescription)
    instance.setTypes(findTypes(ind, ont).toSeq.asJava)
    instance.setNegatedTypes(findNegatedTypes(ind, ont).toSeq.asJava)
  }

  private def findTypes(individual: OWLNamedIndividual, ont: OWLOntology): Set[OntologyClass] =
    findOWLTypes(individual, ont).map(toOntologyClass(_, ont))

  private def findOWLTypes(individual: OWLNamedIndividual, ont: OWLOntology): Set[OWLClass] = for {
    ClassAssertion(_, owlClass: OWLClass, subj) <- ont.getClassAssertionAxioms(individual).asScala.toSet
  } yield owlClass

  private def findNegatedTypes(individual: OWLNamedIndividual, ont: OWLOntology): Set[OntologyClass] =
    findNegatedOWLTypes(individual, ont).map(toOntologyClass(_, ont))

  private def findNegatedOWLTypes(individual: OWLNamedIndividual, ont: OWLOntology): Set[OWLClass] = for {
    ClassAssertion(_, ObjectComplementOf(owlClass: OWLClass), subj) <- ont.getClassAssertionAxioms(individual).asScala.toSet
  } yield owlClass

  private case class Association(subj: OWLNamedIndividual, obj: OWLNamedIndividual)

  private implicit class OptionalOption[T](val self: Optional[T]) extends AnyVal {

    def asScala: Option[T] = if (self.isPresent) Some(self.get) else None

  }

}