package de.boeg.jena.shacl.parse;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CIM2SHACL {

    public static final String END = " ;\n";

    public static void main(String[] args) throws URISyntaxException, IOException {
        var ping = System.currentTimeMillis();
        Map<String, List<SchemaStatement>> map = readSchemaToMap();
        String shapes = generateShapes(map);
        var pong = System.currentTimeMillis();
        System.out.println((pong - ping) + " ms");
        Files.write(Paths.get("./result.ttl"), shapes.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    private static Map<String, List<SchemaStatement>> readSchemaToMap() throws IOException, URISyntaxException {
        var query = Files.readString(Paths.get(ClassLoader.getSystemResource("SchemaSummary.sparql").toURI()));
        var model = ModelFactory.createDefaultModel();
        model.read("EquipmentBoundaryProfileRDFSAugmented-v2_4_15-16Feb2016.rdf");

        var result = QueryExecutionFactory.create(query, model).execSelect();
        var rules = new ArrayList<SchemaStatement>();
        while (result.hasNext()) {
            var rule = SchemaStatement.fromSolution(result.next());
            rules.add(rule);
        }
        return rules.stream().collect(Collectors.groupingBy(SchemaStatement::getClazz));
    }

    private static String generateShapes(Map<String, List<SchemaStatement>> map) {
        StringBuffer sb = new StringBuffer();

        sb.append("PREFIX cim: <http://iec.ch/TC57/2013/CIM-schema-cim16#>\n")
                .append("PREFIX ex: <http://example.com/test#>\n")
                .append("PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n")
                .append("PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>\n")
                .append("PREFIX sh:   <http://www.w3.org/ns/shacl#>\n\n");

        for (var e : map.entrySet()) {
            CIM2SHACL.parseClassShapes(sb, e);
        }

        return sb.toString();
    }

    private static void parseClassShapes(StringBuffer sb, Map.Entry<String, List<SchemaStatement>> entry) {
        var clazz = entry.getKey();

        sb.append("ex:").append(clazz).append("_GenShape\n")
                .append("\trdf:typ sh:NodeShape").append(END)
                .append("\tsh:targetClass cim:").append(clazz).append(END);

        for (var r : entry.getValue()) {
            CIM2SHACL.parsePropertyShape(sb, r);
        }

        sb.append("\tsh:closed true ;\n")
                .append("\tsh:ignoredProperties ( rdf:type ) .\n\n");
    }

    private static void parsePropertyShape(StringBuffer sb, SchemaStatement r) {
        sb.append("\tsh:property [\n")
                .append("\t\tsh:path ").append(r.property).append(END)
                .append("\t\tsh:maxCount ").append(r.maxCard).append(END)
                .append("\t\tsh:minCount ").append(r.minCard).append(END);

        if (!r.datatype.isBlank()) {
            sb.append("\t\tsh:datatype ").append(r.datatype).append(END);
        }
        if (!r.range.isBlank()) {
            sb.append("\t\tsh:nodeKind ex:").append(r.range).append("_GenShape").append(END);
        }

        sb.append("\t]").append(END);
    }
}
