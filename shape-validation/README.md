# RDF ShapeValidation with Apache Jena
Here I show how easly simple RDFData validations can be done with [SHACL](https://www.w3.org/TR/shacl/)

## Project Setup
We need two dependencies to archive this, the [Apache Jena Core](https://jena.apache.org/documentation/javadoc/jena/) and the [SHACL extension](https://jena.apache.org/documentation/javadoc/shacl/)
```
<dependency>
    <groupId>org.apache.jena</groupId>
    <artifactId>jena-core</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.jena</groupId>
    <artifactId>jena-shacl</artifactId>
</dependency>
```

### Test Data Instances
We have Bob who is perfectly fine
```
ex:Bob
    rdf:type     ex:Person ;
    ex:firstName "Bob" ;
    ex:lastName  "Bobson" ;
    ex:nickName  "the guy" ;
    ex:age       "100"^^xsd:integer .
```

And then we have Sue who has some violations
```
ex:Sue
    rdf:type     ex:Person ;
    ex:firstName "Sue" ;
    ex:firstName "Süe" ;
    ex:nickName  "first nicke" ;
    ex:nickName  "second nick" ;
    ex:age       "99" ;
    ex:undifined "undefined" .
```

The data can be seen [here](src/main/resources/data.ttl) and the RDFSchema is given [here](src/main/resources/schema.ttl)

### Test Data Shape
We define a simple Person dataset with the very restrictive shape:

* A Person must have one firstName and lastName property, with type String.

* A Person can have 0..n nickName properties of type String.

* A Person must have am age of type Integer

* A Person should not have any other property.

To archive that we define the SHACL property **sh:minCount** and **sh:maxCount** together with **sh:datatype**.
The shapes can be seen [here](src/main/resources/shapes.ttl)

## Validation with Apache Jena
Sorurces see [ValidateRDFWithSHACL.java](src/main/java/de/boeg/jena/shex/ValidateRDFWithSHACL.java).

First of all we need to load the **data.ttl** and **shapes.ttl** into a Graph, as seen in lines 21 - 27

Then we parse the shapes graph into a Shapes object instance, see line 29.

We validate the the whole graph by passing the shapes and data graph to a ShaclValidator instance, see line 40.

We can even validate single nodes as done in line 46.

The generated report is again RDF data, it is an empty graph if the data is conform or contains a validations report. See method in line 49ff
