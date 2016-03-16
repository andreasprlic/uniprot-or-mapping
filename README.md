# UniProt-Or-Mapping
An *object relational* mapping of *UniProtKB* to a database (and Java objects) using Hibernate.

## Auto-generate Java code and a relational database to manage UniProt data.

This projects uses the [UniProt XML schema](http://www.uniprot.org/docs/uniprot.xsd) to auto-generate Java code as well as a relational database mapping.

## Populate the database and load UniProt data.

Once the initial code/database have been created any UniProt-XML file can get parsed and loaded into the database.

## Default: Load all of SWISSPROT

The default functionality provided here is to load all SWISSPROT entries into the database (see the ``LoadMissing.java`` class). Currently these are about 550k UniProt entries. This process takes about 8 hours on a typical database server.




