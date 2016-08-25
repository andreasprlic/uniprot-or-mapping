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



## Populate the database and load UniProt data.

Once the initial code/database have been created any UniProt-XML file can get parsed and loaded into a database (however the code also works stand-alone off UniProt XML files). 

## Default: Load all of Swiss-Prot

The default functionality provided here is to load all [Swiss-Prot](http://web.expasy.org/docs/swiss-prot_guideline.html) entries into the database (see the ``LoadMissing.java`` class).
Currently these includes about 550k UniProt entries. The process to load all these files takes about 8 hours on a typical database server.


## Get Started

  1. Check out the code
  2. Make sure you have access to a MySQL installation, create an empty new database there, make sure you have write permissions
  3. Update the configuration in ```src/main/resources/database.properties``` to match your configuration
  4. Run ```LoadMissing.java``` (ideally over-night, by next morning you will have a populated database)

Alternative, if you compile the code with ```mvn package```, you will get an executable jar file. You can run this jar file and pass in the DB configuration as a command line parameter (see ```LoadMissing.java``` as the main class).   

## Isoform mapping to alternative transcripts

This project can map UniProt isoforms to alternative transcripts using BioJava. For an example see [isoforms.md](isoforms.md)



## Use Case

A database that is maintained using this code base is used for the UniProt section of the RCSB PDB Protein Feature View that is available from e.g.
[here](http://www.rcsb.org/pdb/protein/P12497).

## Still TODO

  Release on Maven Central


