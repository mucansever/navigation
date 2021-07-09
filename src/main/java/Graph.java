import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
    private HashMap<Long, Vertex> vertices;
    private HashMap<Vertex, ArrayList<Edge>> adj;

    // Constructor
    public Graph() {
        adj = new HashMap<>();
        vertices = new HashMap<>();
    }

    // Add vertex to graph
    public void addVertex(Vertex vertex) {
        adj.put(vertex, new ArrayList<>());
        vertices.put(vertex.getId(), vertex);
    }

    // Returns vertex with given id
    public Vertex getVertex(Long vertexId) {
        return vertices.get(vertexId);
    }

    // Add edge to graph
    public void addEdge(Edge edge) {
        adj.get(edge.getSource()).add(edge);
    }

    // Returns edges from given vertex
    public ArrayList<Edge> edgesFrom(Vertex vertex) {
        return adj.get(vertex);
    }

    // Returns array of vertices in the graph
    public Vertex[] getVertices() {
        return vertices.values().toArray(new Vertex[0]);
    }

    // Removes vertex with given id
    public void removeVertex(Long vertexId) {
        Vertex removed = vertices.remove(vertexId);
        adj.remove(removed);
    }
}
