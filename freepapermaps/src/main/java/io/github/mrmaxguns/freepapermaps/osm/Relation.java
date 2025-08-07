package io.github.mrmaxguns.freepapermaps.osm;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Relation extends OSMElement {
    private final List<Member> members = new ArrayList<>();

    public Relation(long id, boolean visible) {
        super(id, visible);
    }

    public static Relation fromXML(Element rawRelation) throws UserInputException {
        return fromXML(rawRelation, new XMLTools());
    }

    public static Relation fromXML(Element rawRelation, XMLTools xmlTools) throws UserInputException {
        long id = getIdFromXML(rawRelation, xmlTools);
        boolean visible = getVisibleFromXML(rawRelation, xmlTools);
        Relation newRelation = new Relation(id, visible);


        NodeList children = rawRelation.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            if (children.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }

            Element child = (Element) children.item(i);
            if (child.getTagName().equals("member")) {
                String rawType = xmlTools.getAttributeValue(child, "type");
                Member.Type type;
                switch (rawType) {
                    case "node" -> type = Member.Type.Node;
                    case "way" -> type = Member.Type.Way;
                    case "relation" -> type = Member.Type.Relation;
                    default -> throw new UserInputException(
                            "Encountered an invalid relation member type '" + rawType + "'.");
                }

                long ref = xmlTools.getAttributeValueLong(child, "ref");
                String role = xmlTools.getAttributeValue(child, "role");

                newRelation.addMember(new Member(type, ref, role));
            } else if (child.getTagName().equals("tag")) {
                // Parse tags
                newRelation.getTags()
                        .put(xmlTools.getAttributeValue(child, "k"), xmlTools.getAttributeValue(child, "v"));
            }
        }

        return newRelation;
    }

    public List<Member> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(Member member) {
        members.add(member);
    }

    public void clearMembers() {
        members.clear();
    }

    public record Member(Type type, long ref, String role) {
        enum Type {Node, Way, Relation}
    }
}
