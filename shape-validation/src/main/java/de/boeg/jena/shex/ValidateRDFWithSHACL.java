package de.boeg.jena.shex;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

/**
 * Simple SHACL (https://www.w3.org/TR/shacl/) based validation of data
 *
 * @see ShaclValidator Rule validator
 * @see Shapes Rules to validate
 * @see ValidationReport Report generated
 */
public class ValidateRDFWithSHACL {
    public static void main(String[] args) {
        String SHAPES = "shapes.ttl";
        String DATA = "data.ttl";

        Graph dataGraph = RDFDataMgr.loadGraph(DATA);

        // load the shapes into a normal model, should also be done via model.getGraph()
        Graph shapesGraph = RDFDataMgr.loadGraph(SHAPES);
        // parse the shapes from rdf data to "rules"
        Shapes shapes = Shapes.parse(shapesGraph);

        /*
         * Validation from here
         */

        ShaclValidator validator = ShaclValidator.get();

        System.out.println("#############################");
        System.out.println("Report of the validation for the whole graph");
        System.out.println("#############################");
        showResult(validator.validate(shapes, dataGraph));

        System.out.println("#############################");
        System.out.println("Report of the validation only for Bob");
        System.out.println("#############################");
        Node bob = dataGraph.find(Node.ANY, Node.ANY, NodeFactory.createLiteral("Bob")).next().getMatchSubject();
        showResult(validator.validate(shapes, dataGraph, bob));
    }

    private static void showResult(ValidationReport report) {
        if (report.conforms()) {
            System.out.println("Data is conform to rules!");
        } else {
            System.out.println("Data is not conform to rules, here are the violations:");
            ShLib.printReport(report);
        }
    }
}
