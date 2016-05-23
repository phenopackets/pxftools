package org.phenopackets.pxftools

import java.util.{ List => JList }
import org.phenopackets.api.PhenoPacket
import org.specs2.matcher._
import org.phenopackets.api.model.association.DiseaseOccurrenceAssociation
import org.phenopackets.api.model.condition.DiseaseOccurrence
import org.phenopackets.api.model.evidence.Evidence
import org.phenopackets.api.model.condition.DiseaseStage
import org.phenopackets.api.model.evidence.Publication
import org.phenopackets.api.model.entity.Disease
import org.phenopackets.api.model.association.EnvironmentAssociation
import org.phenopackets.api.model.entity.Organism
import org.phenopackets.api.model.entity.Person
import org.phenopackets.api.model.association.PhenotypeAssociation
import org.phenopackets.api.model.association.VariantAssociation
import org.phenopackets.api.model.entity.Variant

trait PhenoPackeCollectiontMatchers extends MustMatchers {
  import scala.collection.JavaConverters._
  def beTheSameDiseaseOccurrenceAssociationList(list1: List[DiseaseOccurrenceAssociation]): Matcher[List[DiseaseOccurrenceAssociation]] =
    ((list2: List[DiseaseOccurrenceAssociation]) => forallWhen(list1 zip list2) { case (l1, l2) => l1 must beEqualTo(l2) })

  def beTheSameEvidenceList(list1: scala.collection.immutable.List[Evidence]): Matcher[scala.collection.immutable.List[Evidence]] =
    ((list2: List[Evidence]) => forallWhen(list1 zip list2) { case (l1, l2) => l1 must beEqualTo(l2) })

  def beTheSameDiseaseListInAnyOrder(list1: List[Disease]): Matcher[List[Disease]] = {
    ((list2: List[Disease]) =>
      (list1.size must beEqualTo(list2.size)) and (
        forallWhen(list1) { case (l1) => list2 must contain(l1) }))
  }

  def beTheSameEnvironmentAssociationList(list1: List[EnvironmentAssociation]): Matcher[List[EnvironmentAssociation]] =
    ((list2: List[EnvironmentAssociation]) => forallWhen(list1 zip list2) { case (l1, l2) => l1 must beEqualTo(l2) })

  def beTheSameOrganismList(list1: List[Organism]): Matcher[List[Organism]] =
    ((list2: List[Organism]) => forallWhen(list1 zip list2) { case (l1, l2) => l1 must beEqualTo(l2) })

  def beTheSamePersonList(list1: List[Person]): Matcher[List[Person]] =
    ((list2: List[Person]) => forallWhen(list1 zip list2) { case (l1, l2) => l1 must beEqualTo(l2) })

  def beTheSamePhenotypeAssociationList(list1: List[PhenotypeAssociation]): Matcher[List[PhenotypeAssociation]] =
    ((list2: List[PhenotypeAssociation]) => forallWhen(list1 zip list2) { case (l1, l2) => l1 must beEqualTo(l2) })

  def beTheSameVariantAssociationList(list1: List[VariantAssociation]): Matcher[List[VariantAssociation]] =
    ((list2: List[VariantAssociation]) => forallWhen(list1 zip list2) { case (l1, l2) => l1 must beEqualTo(l2) })

  def beTheSameVariantListInAnyOrder(list1: List[Variant]): Matcher[List[Variant]] =
    ((list2: List[Variant]) =>
      (list1.size must beEqualTo(list2.size)) and (
        forallWhen(list1) { case (l1) => list2 must contain(l1) }))
}

object PhenoPackeCollectiontMatchers extends PhenoPackeCollectiontMatchers