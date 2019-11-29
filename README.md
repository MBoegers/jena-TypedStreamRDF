# Read and type RDF Data via Apache Jena
We use the [StreamRDF](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/riot/system/StreamRDF.html)
with [RDFDataMgr](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/riot/RDFDataMgr.html)
from [Apache Jena ARQ 3.13.1](https://mvnrepository.com/artifact/org.apache.jena/jena-arq/3.13.1) and
read the RDF data from a file into an Apache Jena Dataset.

To determine the types of the literals we use a map to store the information which datatype is to chose for the given triple.
We use the following RDFS entailment pattern:

```
?s ?p ?o.
?p rdf:type rdfs:Property;
    rdfs:domain ?t.
    
=> ?o rdf:type ?t.
```

## Further work
A small utility with which the map can be generated from a RDFS file may be added soon