# Load into a database

It is not necessary to load UniProt into a database (the code works also when just using UniProt XML files). However it is a possibility provided by this project. This has the advantage that queries can be formulated across all of UniProt.

There are two possibilities how to load into a database.
 1) from the IDE,
 2) from the command line, using a jar file
 
## 1) Load from IDE:
  1. Make sure you have access to a MySQL installation, create an empty new database there, make sure you have write permissions
  2. Update the configuration in ```src/main/resources/database.properties``` to match your configuration
  3. Run ```LoadMissing.java``` (ideally over-night, by next morning you will have a populated database)

## 2) Load from a jar file

An executable jar file can be compiled with ```mvn package```. You can run this jar file and pass in the DB configuration 
as a command line parameter (see ```LoadMissing.java``` as the main class).   

## Default: Load all of Swiss-Prot

The default functionality provided here is to load all [Swiss-Prot](http://web.expasy.org/docs/swiss-prot_guideline.html) entries into the database (see the ``LoadMissing.java`` class).
Currently these includes about 550k UniProt entries. The process to load all these files takes about 8 hours on a typical database server.
