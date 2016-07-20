package org.phenopackets.pxftools.util

import com.hp.hpl.jena.rdf.model.ResourceFactory

object PhenoPacketVocabulary {

  private val Pheno = "http://phenopackets.org"
  private val DC = "http://purl.org/dc/terms"

  private def p = ResourceFactory.createProperty(_: String)

  val Diseases = p(s"$Pheno/diseases")
  val PhenotypeProfile = p(s"$Pheno/phenotype_profile")
  val Entity = p(s"$Pheno/entity")
  val Phenotype = p(s"$Pheno/phenotype")
  val Onset = p(s"$Pheno/onset")
  val Frequency = p(s"$Pheno/frequency")
  val Evidence = p(s"$Pheno/evidence")
  val Description = p(s"$DC/description")
  val Source = p(s"$DC/source")
  val Contributor = p(s"$DC/contributor")
  val Date = p(s"$DC/date")
  val OWLComplementOf = p("http://www.w3.org/2002/07/owl#complementOf")

}