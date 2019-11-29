package de.boeg.jena.read;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This implementation of an StreamRDF reads triple and uses a map too type them with an XSDDatatype.
 * The result will be a dataset with all found graphs.
 * The Stream should be used with the RDFDataMgr.
 *
 * @author Merlin Bögershausen merlin.boegershausen@rwth-aachen.de
 * @see XSDDatatype
 * @see org.apache.jena.riot.RDFDataMgr
 */
public class TypedStreamRDF implements StreamRDF {

    private final Map<Property, XSDDatatype> datatypeMapping = new HashMap<>();
    private final Map<Node, Graph> sink = new HashMap<>();
    private final Map<String, String> prefixMapping = new HashMap<>();
    private String base = "";
    private Dataset dataset;

    public Dataset getDataset() {
        return dataset;
    }

    /**
     * Configure the Stream with the applied datatype map
     *
     * @param datatypeMapping rdf:Property to XSDDatatype
     */
    public TypedStreamRDF(Map<Property, XSDDatatype> datatypeMapping) {
        this.datatypeMapping.putAll(datatypeMapping);
    }

    /**
     * Adds the empty default graph to the sink
     */
    public void start() {
        sink.putIfAbsent(Quad.defaultGraphNodeGenerated, GraphFactory.createDefaultGraph());
    }

    /**
     * Reader found a triple, we type and add it to the default graph
     *
     * @param triple new triple
     */
    public void triple(Triple triple) {
        Triple typedTriple = typeTriple(triple);
        addToGraphs(Quad.defaultGraphNodeGenerated, typedTriple);
    }

    /**
     * Reader found a triple, we type and add it to the respective graph
     *
     * @param quad new quad (triple with graph)
     */
    public void quad(Quad quad) {
        Triple typedTriple = typeTriple(quad.asTriple());
        Node graphRef = quad.getGraph();
        addToGraphs(graphRef, typedTriple);
    }

    /**
     * Read found the base of our dataset
     *
     * @param base base value
     */
    public void base(String base) {
        this.base = base;
    }

    /**
     * Reader found prefix mappings for our dataset
     *
     * @param prefix new placeholder
     * @param iri    iti to replace
     */
    public void prefix(String prefix, String iri) {
        this.prefixMapping.put(prefix, iri);
    }

    /**
     * Reader finished reading, we assemble the dataset from the found graphs
     */
    public void finish() {
        this.dataset = DatasetFactory.create();
        for (Node graphRef : sink.keySet()) {
            Graph graph = sink.get(graphRef);
            Model model = ModelFactory.createModelForGraph(graph);
            if (Quad.isDefaultGraph(graphRef)) { /* add default graph via correct method */
                dataset.setDefaultModel(model);
            } else {
                String graphName = graphRef.getURI().replace(base, "");
                dataset.addNamedModel(graphName, model);
            }
        }
    }

    /**
     * If the triple contains a literal we extract that and apply the typing, otherwise the original triple is returned
     *
     * @param triple triple to type
     * @return typed literal or original
     */
    private Triple typeTriple(Triple triple) {
        if (triple.getMatchObject().isLiteral()) {// only apply typing of object is literal
            /* determine datatype, use xsd:string as default */
            XSDDatatype datatype = datatypeMapping.getOrDefault(triple.getPredicate(), XSDDatatype.XSDstring);
            /* generate typed literal */
            String literalValue = triple.getObject().getLiteralLexicalForm();
            Literal typedLiteral = ResourceFactory.createTypedLiteral(literalValue, datatype);
            /* generate new triple */
            triple = new Triple(triple.getSubject(), triple.getPredicate(), typedLiteral.asNode());
        }
        return triple;
    }

    /**
     * Add the triple to the referred graph. If the graph is not present, at it.
     *
     * @param graphRef graph to at the triple to
     * @param triple   triple to add
     */
    private void addToGraphs(Node graphRef, Triple triple) {
        /* get graph from sink */
        Graph graph = sink.getOrDefault(graphRef, GraphFactory.createDefaultGraph());
        /* add triple */
        graph.add(triple);
        /* add changed graph to sink */
        sink.put(graphRef, graph);
    }
}
