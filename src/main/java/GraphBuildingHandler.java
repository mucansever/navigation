import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;


public class GraphBuildingHandler extends DefaultHandler {


    public static class Way {
        private String id;
        private String name;
        private String speed;
        private boolean isOneWay;

        private final ArrayList<Long> listOfNodes = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public ArrayList<Long> getListOfNodes() {
            return listOfNodes;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setOneWay(boolean oneway) {
            this.isOneWay = oneway;
        }

        public boolean isOneWay() {
            return this.isOneWay;
        }
    }

    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));

    private String activeState = "";
    private final GraphDB g;
    private Way currentWay = new Way();
    private Vertex lastSavedVertex;
    private boolean flag = false;

    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("node")) {
            activeState = "node";
            Vertex vertex = new Vertex(
                    Double.parseDouble(attributes.getValue("lat")),
                    Double.parseDouble(attributes.getValue("lon")),
                    Long.parseLong(attributes.getValue("id")));

            g.graph.addVertex(vertex);
            lastSavedVertex = vertex;
        } else if (qName.equals("way")) {
            activeState = "way";
            // Start the way element
            currentWay = new Way();
            currentWay.setId(attributes.getValue("id"));
        } else if (activeState.equals("way") && qName.equals("nd")) {
            currentWay.getListOfNodes().add(Long.parseLong(attributes.getValue("ref")));
        } else if (activeState.equals("way") && qName.equals("tag")) {
            // Handles tags for ways
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            switch (k) {
                case "maxspeed":
                    // Set the speed for the current way
                    currentWay.setSpeed(v);
                    break;
                case "highway":
                    // Mark flag as true if this way should be used to connect vertices in the graph.
                    if (ALLOWED_HIGHWAY_TYPES.contains(v))
                        flag = true;
                    break;
                case "name":
                    // Set the name for the current way
                    currentWay.setName(v);
                    break;
                case "oneway":
                    // Set the oneway property for the current way
                    currentWay.setOneWay(v.equals("yes"));
                    break;
            }
        } else if (activeState.equals("node") && qName.equals("tag") && attributes.getValue("k")
                .equals("name")) {
            String normalizedName = GraphDB.normalizeString(attributes.getValue("v"));
            lastSavedVertex.setName(attributes.getValue("v"));
            g.tst.put(normalizedName, lastSavedVertex);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            if (!flag)
                return;
            ArrayList<Long> nodes = currentWay.getListOfNodes();
            for (int i=1; i<nodes.size(); i++) {
                Edge newEdge = new Edge();
                Vertex source = g.graph.getVertex(nodes.get(i-1));
                Vertex dest = g.graph.getVertex((nodes.get(i)));
                newEdge.setSource(source);
                newEdge.setDestination(dest);
                newEdge.setSpeed(currentWay.getSpeed());
                newEdge.setName(currentWay.getName());
                newEdge.setWeight(g.distance(source,dest));
                g.graph.addEdge(newEdge);
                if (!currentWay.isOneWay()) {
                    Edge newEdgeRev = new Edge();
                    newEdgeRev.setSource(dest);
                    newEdgeRev.setDestination(source);
                    newEdgeRev.setSpeed(currentWay.getSpeed());
                    newEdgeRev.setName(currentWay.getName());
                    newEdgeRev.setWeight(g.distance(source,dest));
                    g.graph.addEdge(newEdgeRev);
                }
            }
            currentWay = new Way();
            flag = false;
        }
    }
}