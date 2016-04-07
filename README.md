# PXFtools command line utility

This project provides a command line utility for operating over Phenotype eXchange Format (PXF) files.

## Usage output
```
Usage

 pxftools [options] command [command options]

Options

   --format=STRING : Output format. Set the output format to one of:
                     yaml
                     json
   --out=STRING    : Output file. Omit to write to standard out.

Commands

   convert [command options] : Read in a PXF file and output in the specified format.
      --in=STRING : Input file. Pass '-' or omit to read from standard in.

   merge <files> ... : Read in multiple PXF files and output as a single merged PXF file in the specified format.
```
