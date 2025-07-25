package io.github.mrmaxguns.freepapermaps.styling;

import io.github.mrmaxguns.freepapermaps.UserInputException;
import io.github.mrmaxguns.freepapermaps.XMLTools;
import io.github.mrmaxguns.freepapermaps.osm.TagList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;


public class TagQuery {
    private final QueryOperator root;

    public TagQuery(QueryOperator root) {
        this.root = root;
    }

    public static TagQuery fromXML(Element rootElement) throws UserInputException {
        return fromXML(rootElement, new XMLTools());
    }

    public static TagQuery fromXML(Element rootElement, XMLTools xmlTools) throws UserInputException {
        Stack<QueryOperator> stack = new Stack<>();
        HashMap<Element, QueryOperator> elementQueryOpMapping = new HashMap<>();
        HashMap<QueryOperator, Element> queryOpElementMapping = new HashMap<>();
        HashMap<QueryOperator, QueryOperator> parentMapping = new HashMap<>();
        HashMap<QueryOperator, Boolean> visited = new HashMap<>();

        // It is implied that the outermost <node>/<way> is an <and> operator.
        QueryOperator rootOp = new And();

        // We are setting up a DFS traversal here. The goal is to take the nested XML hierarchy (which can be viewed
        // as a tree) and convert it into QueryOperators.
        stack.push(rootOp);
        // There is a 1:1 correspondence between an Element and QueryOperator, and we need to keep track of it both ways
        elementQueryOpMapping.put(rootElement, rootOp);
        queryOpElementMapping.put(rootOp, rootElement);
        parentMapping.put(rootOp, null); // Keep track of each node's parent
        visited.put(rootOp, false);

        while (!stack.isEmpty()) {
            QueryOperator currentOp = stack.pop();

            if (visited.get(currentOp)) {
                continue;
            }

            visited.put(currentOp, true);

            // We add the parent to the stack (if we're not at the root) since DFS graph traversal requires all adjacent
            // vertices to be added to the stack.
            if (parentMapping.get(currentOp) != null) {
                stack.push(currentOp);
            }

            NodeList children = queryOpElementMapping.get(currentOp).getChildNodes();
            for (int i = 0; i < children.getLength(); ++i) {
                if (children.item(i).getNodeType() != Node.ELEMENT_NODE) { continue; }
                Element childElement = (Element) children.item(i);

                QueryOperator childOp;
                if (elementQueryOpMapping.containsKey(childElement)) {
                    // We already parsed this query, so we deal with it normally
                    childOp = elementQueryOpMapping.get(childElement);
                } else {
                    // We haven't parsed this query, so we need to add it to all the mappings first
                    childOp = parseElement(childElement, xmlTools);
                    elementQueryOpMapping.put(childElement, childOp);
                    queryOpElementMapping.put(childOp, childElement);
                    parentMapping.put(childOp, currentOp);
                    visited.put(childOp, false);
                    currentOp.children.add(childOp);
                }

                stack.push(childOp);
            }
        }

        return new io.github.mrmaxguns.freepapermaps.styling.TagQuery(rootOp);
    }

    private static QueryOperator parseElement(Element el, XMLTools xmlTools) throws UserInputException {
        switch (el.getTagName()) {
            case "tag" -> {
                return new TagQueryOperator(xmlTools.getAttributeValue(el, "k"), xmlTools.getAttributeValue(el, "v"));
            }
            case "and" -> {
                return new And();
            }
            case "or" -> {
                return new Or();
            }
            case "not" -> {
                return new Not();
            }
            default -> throw new UserInputException(
                    "When processing query expected one of 'tag', 'and', 'not', 'or', but got " + el.getTagName() +
                    "'.");
        }
    }

    public boolean matches(TagList tags) {
        return root.matches(tags);
    }

    public static abstract class QueryOperator {
        public final ArrayList<QueryOperator> children;

        protected QueryOperator(ArrayList<QueryOperator> children) { this.children = children; }

        public abstract boolean matches(TagList tags);
    }


    public static class And extends QueryOperator {
        public And() { super(new ArrayList<>()); }

        public boolean matches(TagList tags) {
            return children.stream().allMatch(i -> i.matches(tags));
        }
    }


    public static class Not extends QueryOperator {
        public Not() { super(new ArrayList<>()); }

        public boolean matches(TagList tags) {
            return !children.stream().allMatch(i -> i.matches(tags));
        }
    }


    public static class Or extends QueryOperator {
        public Or() { super(new ArrayList<>()); }

        public boolean matches(TagList tags) {
            return children.stream().anyMatch(i -> i.matches(tags));
        }
    }


    public static class TagQueryOperator extends QueryOperator {
        public final String tagKey;
        public final String tagValue;

        public TagQueryOperator(String tagKey, String tagValue) {
            super(null);
            this.tagKey = tagKey.toLowerCase();
            this.tagValue = tagValue.toLowerCase();
        }

        public boolean matches(TagList tags) {
            if (!tags.containsKey(tagKey)) { return false; }
            String otherValue = tags.get(tagKey);

            if (tagValue.isEmpty()) {
                return true;
            }

            return otherValue.toLowerCase().equals(tagValue);
        }
    }
}
