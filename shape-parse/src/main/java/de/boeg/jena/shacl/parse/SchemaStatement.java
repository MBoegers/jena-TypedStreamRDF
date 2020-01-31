package de.boeg.jena.shacl.parse;

import org.apache.jena.query.QuerySolution;

public class SchemaStatement {

    final String clazz;
    final String property;
    final String minCard;
    final String maxCard;
    final String range;
    final String datatype;

    private SchemaStatement(String clazz, String property, String minCard, String maxCard, String range, String datatype) {
        this.clazz = clazz;
        this.property = property;
        this.minCard = minCard;
        this.maxCard = maxCard;
        this.range = range;
        this.datatype = datatype;
    }

    public static SchemaStatement fromSolution(QuerySolution qs) {
        var clazz = qs.get("class").toString().split("#")[1];
        var path = "cim:".concat(qs.get("path").toString().split("#")[1]);
        var card = qs.get("cardinality").toString().split("M:")[1];
        var minCard = card.length() > 1 ? String.valueOf(card.charAt(0)) : "0";
        var maxCard = card.length() > 1 ? String.valueOf(card.charAt(3)) : "1000";
        var rang = qs.contains("range") ? qs.get("range").toString().split("#")[1] : "";
        var datatype = qs.contains("datatype") ? mapDatatype(qs.get("datatype").toString().split("#")[1]) : "";

        return new SchemaStatement(clazz, path, minCard, maxCard, rang, datatype);
    }

    private static String mapDatatype(String d) {
        switch (d) {
            case "Date":
                return "xsd:date";
            case "Voltage":
            case "Float":
                return "xsd:decimal";
            case "Boolean":
                return "xsd:boolean";
            case "String":
            default:
                return "xsd:string";
        }
    }

    public String getClazz() {
        return clazz;
    }
}
