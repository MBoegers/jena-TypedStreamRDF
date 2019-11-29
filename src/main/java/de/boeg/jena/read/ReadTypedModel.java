package de.boeg.jena.read;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReadTypedModel {

    public static void main(String[] args) {
        Path dataFile = Paths.get(""); // set this to you input

        Map<Property, XSDDatatype> typeMap = new HashMap<>(); // put all mappings property -> XSDDatatype via typMap.put(property, dtype)
        String baseURL = ""; // set this to youe base
        Lang lang = Lang.RDFXML; // change if you use a different language

        /* trigger the typed reading of the dataset */

        Dataset dataset = null;
        try (InputStream is = Files.newInputStream(dataFile, StandardOpenOption.READ)) { // open input stream
            TypedStreamRDF sink = new TypedStreamRDF(typeMap); // configure typed stream
            RDFDataMgr.parse(sink, is, baseURL, lang); // pass everything to the RDFDataMgr
            dataset = sink.getDataset(); // receive the dataset
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("We have following models in our dataset:");
        Iterator<String> graphNamesIt = dataset.listNames();
        System.out.println("\t- default with " + dataset.getDefaultModel().size() + " triples");
        while(graphNamesIt.hasNext()) {
            String name = graphNamesIt.next();
            System.out.println("\t- " + name + " with " + dataset.getNamedModel(name).size() + " triples");
        }
        /* done! do your work with the dataset */
    }
}
