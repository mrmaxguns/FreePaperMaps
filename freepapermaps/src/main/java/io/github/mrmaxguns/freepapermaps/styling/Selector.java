package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.geometry.Geometry;
import org.w3c.dom.Element;

import java.util.Objects;


public class Selector {
    private final TagQuery query;
    private String id;
    public Type type;


    public Selector(String id, Type type, TagQuery query) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.query = query;
    }


    ;

    public static Selector fromXML(Element rawSelector, XMLTools xmlTools) throws UserInputException {
        Type type;
        switch (rawSelector.getTagName()) {
            case "node" -> type = Type.Node;
            case "way" -> type = Type.Way;
            case "polygon" -> type = Type.Polygon;
            default -> throw new UserInputException("Undefined selector '" + rawSelector.getTagName() + "'.");
        }

        String id = xmlTools.getAttributeValue(rawSelector, "id");
        TagQuery query = TagQuery.fromXML(rawSelector, xmlTools);
        return new Selector(id, type, query);
    }

    public boolean matches(Geometry val) {
        return query.matches(val.getTags());
    }

    public enum Type {Node, Way, Polygon}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public TagQuery getQuery() {
        return query;
    }
}
