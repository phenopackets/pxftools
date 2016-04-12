package org.phenopackets.pxftools

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary
import org.phenopackets.api.PhenoPacket
import org.phenopackets.api.model.entity.Disease
import org.phenopackets.api.model.condition.DiseaseOccurrence
import org.phenopackets.api.model.condition.DiseaseStage
import org.phenopackets.api.model.association.DiseaseOccurrenceAssociation
import org.phenopackets.api.model.ontology.OntologyClass
import org.phenopackets.api.model.environment.Environment
import scala.collection.JavaConverters._
import org.phenopackets.api.model.evidence.Evidence
import org.phenopackets.api.model.evidence.Publication
import org.phenopackets.api.model.association.EnvironmentAssociation
import org.phenopackets.api.model.entity.Organism
import org.phenopackets.api.model.entity.Person
import org.phenopackets.api.model.association.PhenotypeAssociation
import org.phenopackets.api.model.condition.Phenotype
import org.phenopackets.api.model.entity.Variant

object PhenoPacketGenerator {

  def genDiseaseStage: Gen[DiseaseStage] = {
    for {
      description <- arbitrary[String]
    } yield {
      val stage = new DiseaseStage()
      stage.setDescription(description)
      stage
    }
  }

  def genDiseaseOccurrence: Gen[DiseaseOccurrence] = {
    for {
      stage <- genDiseaseStage
    } yield {
      val occurrence = new DiseaseOccurrence()
      occurrence.setStage(stage)
      occurrence
    }
  }

  def genDiseaseOccurrenceAssociation: Gen[DiseaseOccurrenceAssociation] = {
    for {
      occurence <- genDiseaseOccurrence
    } yield {
      new DiseaseOccurrenceAssociation.Builder(occurence).build()
    }
  }

  def genDisease: Gen[Disease] = {
    for {
      id <- arbitrary[String]
      label <- arbitrary[String]
    } yield {
      val disease = new Disease()
      disease.setId(id)
      disease.setLabel(label)
      disease
    }
  }

  def genOntologyClass: Gen[OntologyClass] = {
    for {
      id <- arbitrary[String]
      label <- arbitrary[String]
    } yield {
      new OntologyClass.Builder(id).setLabel(label).build()
    }

  }

  def genEnvironment: Gen[Environment] = {
    for {
      ontologyClass <- genOntologyClass
      ontologyClass2 <- genOntologyClass
      negatedOntologyClass <- genOntologyClass
      negatedOntologyClass <- genOntologyClass
      description <- arbitrary[String]
    } yield {
      val environment = new Environment()
      environment.setDescription(description)
      environment.setTypes(List(ontologyClass, ontologyClass2).asJava)
      environment.setNegatedTypes(List(negatedOntologyClass, negatedOntologyClass).asJava)
      environment
    }
  }

  def genPublication: Gen[Publication] = {
    for {
      id <- arbitrary[String]
    } yield {
      new Publication.Builder().setId(id).build()
    }
  }

  def genEvidence: Gen[Evidence] = {
    for {
      type1 <- genOntologyClass
      type2 <- genOntologyClass
      pub1 <- genPublication
      pub2 <- genPublication
      label <- arbitrary[String]
    } yield {
      val evidence = new Evidence()
      evidence.setTypes(List(type1, type2).asJava)
      evidence.setSupportingPublications(List(pub1, pub2).asJava)
      evidence
    }
  }

  def genEnvironmentAssociation: Gen[EnvironmentAssociation] = {
    for {
      entityId <- arbitrary[String]
      environment <- genEnvironment
      evidence <- genEvidence
    } yield {
      new EnvironmentAssociation.Builder(environment).setEntityId(entityId).addEvidence(evidence).build()
    }
  }

  def genOrganism: Gen[Organism] = {
    for {
      id <- arbitrary[String]
      label <- arbitrary[String]
      taxon <- genOntologyClass
    } yield {
      val organism = new Organism()
      organism.setId(id)
      organism.setLabel(label)
      organism.setTaxon(taxon)
      organism
    }
  }

  def genPerson: Gen[Person] = {
    for {
      id <- arbitrary[String]
    } yield {
      val person = new Person()
      person.setId(id)
      person
    }
  }

  def genPhenotype: Gen[Phenotype] = {
    for {
      typee <- arbitrary[String]
      description <- arbitrary[String]
    } yield {

      val pb = new Phenotype.Builder()
      pb.addType(typee).description(description)
      pb.build()
    }
  }

  def genPhenotypeAssociation: Gen[PhenotypeAssociation] = {
    for {
      person <- genPerson
      phenotype <- genPhenotype
    } yield {
      new PhenotypeAssociation.Builder(phenotype).setEntity(person).build()
    }
  }

  def genVariant: Gen[Variant] = {
    for {
      id <- arbitrary[String]
      label <- arbitrary[String]
      description <- arbitrary[String]
    } yield {
      val variant = new Variant()
      variant.setId(id)
      variant.setLabel(label)
      variant.setDescriptionHGVS(description)
      variant
    }
  }

  def genPhenopacket: Gen[PhenoPacket] = {
    for {
      title <- arbitrary[String]
      disease <- genDisease
      diseaseOccurrenceAssociation <- genDiseaseOccurrenceAssociation
      environmentAssociation <- genEnvironmentAssociation
      organism <- genOrganism
      person <- genPerson
      phenotypeAssociation <- genPhenotypeAssociation
      variant <- genVariant
    } yield {
      val builder = new PhenoPacket.Builder()
      builder.title(title)
      builder.addDisease(disease)
      builder.addDiseaseOccurrenceAssociation(diseaseOccurrenceAssociation)
      builder.addEnvironmentAssociation(environmentAssociation)
      builder.addOrganism(organism)
      builder.addPerson(person)
      builder.addPhenotypeAssociation(phenotypeAssociation)
      builder.addVariant(variant)
      builder.build()
    }
  }

}