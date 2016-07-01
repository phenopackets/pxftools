package org.phenopackets.pxftools.util

import org.apache.jena.rdf.model.ResourceFactory

object PhenoPacketVocabulary {

  private val Pheno = "http://phenopackets.org"
  private val DC = "http://purl.org/dc/terms"

  private def p = ResourceFactory.createProperty(_: String)

  val Diseases = p(s"$Pheno/diseases")
  val PhenotypeProfile = p(s"$Pheno/phenotype_profile")
  val Entity = p(s"$Pheno/entity")
  val Phenotype = p(s"$Pheno/phenotype")
  val Evidence = p(s"$Pheno/evidence")
  val Description = p(s"$DC/description")
  val Source = p(s"$DC/source")

}