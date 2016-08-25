# UniProt-Or-Mapping
An *object relational* mapping of **UniProtKB** to a database and/or Java objects using Hibernate.

## Auto-generate Java code and (optionally) to a relational database to manage UniProt data.

This projects uses the [UniProt XML schema](http://www.uniprot.org/docs/uniprot.xsd) to auto-generate Java code as well as a relational database mapping.

## How to parse a UniProt XML file into a Java data structure:

```java

            URL u = UniProtTools.getURLforXML(accession);
            InputStream inStream = u.openStream();
            Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);
```



## Get Started

  1. Check out the code
  2. After running ```mvn install``` you can immediately parse UniProt XML files.

## Load into a database
Once the initial code/database have been created any UniProt-XML file can get parsed and loaded into a database (however the code also works without a database by just reading UniProt XML files). 

See [here](loaddatabase.md) for how to load into a database.
 
 
## Isoform mapping to alternative transcripts

This project can map UniProt isoforms to alternative transcripts using BioJava. For an example see [isoforms.md](isoforms.md)

## Use Case

A database that is maintained using this code base is used for the UniProt section of the RCSB PDB Protein Feature View that is available from e.g.
[here](http://www.rcsb.org/pdb/protein/P12497).

## Still TODO

  Release on Maven Central


