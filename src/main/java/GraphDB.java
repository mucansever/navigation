import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class GraphDB {


    public Graph graph = new Graph();
    public TST<Vertex> tst = new TST<>();

    /**
     * Construct graph by reading the file and remove unnecessary vertices
     * @param dbPath: path to the XML file with graph information
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Discard non-alphabetical characters of string
     * @param s: input string
     * @return (String) normalized string
     */
    static String normalizeString(String s) {
        String regex = "[^a-zA-Z]";
        return s.replaceAll(regex, "").toLowerCase();
    }

    /**
     * Remove the vertices with no incoming and outgoing connections from graph
     */
    private void clean() {
        ArrayList<Long> removalList = new ArrayList<>();
        HashSet<Long> usedVertices = new HashSet<>();
        // Create 2 lists; one for which vertices with no outgoing edges, and one for vertices with incoming edges
        for (Vertex v: graph.getVertices()) {
            if (graph.edgesFrom(v).size() == 0)
                removalList.add(v.getId());
            else
                for (Edge edge: graph.edgesFrom(v))
                    usedVertices.add(edge.getDestination().getId());
        }
        // Remove vertices
        for (Long id: removalList)
            if (!usedVertices.contains(id))
                graph.removeVertex(id);
    }

    /**
     * Return the euclidean distance between two vertices
     * @param v1: first vertex
     * @param v2: second vertex
     * @return (double) distance
     */
    public double distance(Vertex v1, Vertex v2) {
        double lat = (v1.getLat()-v2.getLat());
        double lon = (v1.getLng()-v2.getLng());
        return Math.sqrt(Math.pow(lat,2)+Math.pow(lon,2));
    }

    /**
     * Returns the closest vertex to the given latitude and longitude values
     * @param lon: longitude value
     * @param lat: latitude value
     * @return (long) vertex id
     */
    public long closest(double lon, double lat) {
        Vertex position = new Vertex(lat,lon,0L);
        double closest = 999999;
        Vertex answer = position;
        for (Vertex vertex: graph.getVertices()) {
            if (distance(vertex, position) < closest) {
                closest = distance(vertex, position);
                answer = vertex;
            }
        }
        return answer.getId();
    }

    /**
     * Returns the longitude of the given vertex, v is the vertex id
     * @param v: vertex id
     * @return (double) longitude
     */
    double lon(long v) {
        return graph.getVertex(v).getLng();
    }

    /**
     * Returns the latitude of the given vertex, v is the vertex id
     * @param v: vertex id
     * @return (double) latitude
     */
    double lat(long v) {
        return graph.getVertex(v).getLat();
    }
}
