package org.phenopackets.pxftools

import org.backuity.clist._
import org.phenopackets.pxftools.command.Convert
import org.phenopackets.pxftools.command.Merge

object Main extends App {

  Cli.parse(args).withProgramName("pxftools").withCommands(Convert, Merge).foreach(_.run())

}