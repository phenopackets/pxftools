[![Build Status](https://travis-ci.org/phenopackets/pxftools.svg?branch=master)](https://travis-ci.org/phenopackets/pxftools)

## CAUTION! THIS REPO HAS BEED RETIRED!

This initial implementation has now been archived - please refer to the [phenopacket-schema](https://github.com/phenopackets/phenopacket-schema) repository for the current implementation.

# PXFtools command line utility

This project provides a command line utility for operating over Phenotype eXchange Format (PXF) files.

## Building

Install `sbt` (Scala Build Tool) on your system. For Mac OS X, it is easily done using [Homebrew](http://brew.sh): `brew install sbt`. `sbt` requires a working Java installation, but you do not need to otherwise install Scala.

`sbt compile`

Development versions of pxftools may depend on the latest snapshot version of the [PhenoPackets Java API](https://github.com/phenopackets/phenopacket-reference-implementation). You may need to install the latest `phenopackets-api` into your local Maven repository before building pxftools.

## Running

You can download a prepackaged [release](https://github.com/phenopackets/pxftools/releases).

To build the command-line executable, run:

`sbt stage`

You will find executables for Unix and Windows in `target/universal/stage/bin/`. These depend on the libraries in `target/universal/stage/lib`.

## Packaging a release

`sbt universal:packageZipTarball`

The release package will be at `target/universal/pxftools-<version>.tgz`

## Usage output
```
Usage

 pxftools [options] command [command options]

Options

   --informat=STRING  : Input format. By default both yaml and json will be attempted. Set the input format to one of:
                        yaml
                        json
                        hpo-phenote
   --out=STRING       : Output file. Omit to write to standard out.
   --outformat=STRING : Output format. Set the output format to one of:
                        yaml
                        json
                        turtle

Commands

   convert <infile> : Read in a PXF file and output in the specified format.

   merge <files> ... : Read in multiple PXF files and output as a single merged PXF file in the specified format.
```
