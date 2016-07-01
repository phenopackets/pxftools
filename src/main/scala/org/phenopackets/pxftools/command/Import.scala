package org.phenopackets.pxftools.command

import org.backuity.clist._
import com.github.tototoshi.csv.CSVReader
import scala.io.Source
import com.github.tototoshi.csv.TSVFormat
import org.phenopackets.pxftools.util.HPOAnnotations

object Import extends Command(description = "Create a PhenoPacket from the input.") with Common with SingleInput {

  var inFormat = opt[String](description = "Input format. One of:\nhpoa\nturtle", default = "turtle")

  override def run(): Unit = {
    val table = CSVReader.open(Source.fromInputStream(determineInput, "utf-8"))(new TSVFormat {})
    writePhenoPacket(HPOAnnotations.importFromTable(table), determineOutput, outputWriter)
  }

}
