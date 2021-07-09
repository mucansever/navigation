import java.util.*;


public class Router {

    private static List<Vertex> stops = new ArrayList<>();
    private static Vertex start, end;

    /**
     * Return the shortest path between start and end points
     * @param g: graph object
     * @param stlon: longitude of start point
     * @param stlat: latitude of start point
     * @param destlon: longitude of destination point
     * @param destlat: latitude of destination point
     * @return list of vertex ids
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat, double destlon, double destlat) {
        // Use g.closest() to get start and end vertices
        start = g.graph.getVertex(g.closest(stlon,stlat));
        end = g.graph.getVertex(g.closest(destlon,destlat));

        LinkedList<Long> answer = new LinkedList<>();
        // Shortest distance to specified node
        HashMap<Vertex,Double> distances = new HashMap<>();
        // Save the last node used to reach specified node
        HashMap<Vertex, Vertex> prev = new HashMap<>();
        LinkedHashSet<Vertex> settledNodes = new LinkedHashSet<>();
        // Priority queue to hold vertices by their distances
        PriorityQueue<Vertex> unsettledNodes = new PriorityQueue<>(Comparator.comparing(distances::get));

        // Give each node high distance
        for (Vertex node: g.graph.getVertices())
            distances.put(node, 999999.0);
        // Add source node to unsettledNodes
        unsettledNodes.add(start);
        distances.put(start, 0.0);

        while (!unsettledNodes.isEmpty()) {
            // Remove the node with lowest distance from unsettledNodes and add it to settledNodes
            Vertex evaluationNode = unsettledNodes.remove();
            settledNodes.add(evaluationNode);
            if (evaluationNode==end) {
                break;
            }
            // Evaluate neighbors
            for (Edge edge: g.graph.edgesFrom(evaluationNode)) {
                Vertex destinationNode = edge.getDestination();
                if (settledNodes.contains(destinationNode))
                    continue;
                double edgeDistance = edge.getWeight();
                double newDistance = distances.get(evaluationNode)+edgeDistance;
                if (distances.get(destinationNode) > newDistance) {
                    distances.put(destinationNode, newDistance);
                    prev.put(destinationNode, evaluationNode);
                    unsettledNodes.add(destinationNode);
                }
            }
        }
        // Construct route by backtracking
        Vertex iterator = end;
        while (iterator != start) {
            if (!prev.containsKey(iterator))
                return new LinkedList<>();
            answer.addFirst(iterator.getId());
            iterator = prev.get(iterator);
        }
        answer.addFirst(start.getId());
        // Return ids of vertices as a linked list
        return answer;
    }

    /**
     * Add a new stop to route and recalculate the route respectively
     * @param g: graph object
     * @param lat: latitude of the new stop
     * @param lon: longitude of the new stop
     * @return list of vertex ids
     */
    public static LinkedList<Long> addStop(GraphDB g, double lat, double lon) {
        // Find the closest vertex to the stop coordinates using g.closest()
        // Add the stop to correct place in stop list
        LinkedList<Long> route = new LinkedList<>();
        Vertex newStop = g.graph.getVertex(g.closest(lon, lat));
        boolean inserted = false;
        for (int i = 0; i < stops.size(); i++) {
            if (g.distance(start, stops.get(i)) >= g.distance(start, newStop)) {
                stops.add(i, newStop);
                inserted = true;
                break;
            }
        }
        if (!inserted)
            stops.add(newStop);

        // Recalculate your route when a stop is added and return the new route
        Vertex originalStart = start;
        Vertex originalEnd = end;
        for (Vertex stop : stops) {
            LinkedList<Long> pathSlice = shortestPath(g, start.getLng(), start.getLat(), stop.getLng(), stop.getLat());
            for (Long l : pathSlice)
                route.addLast(l);
            start = stop;
        }
        LinkedList<Long> pathSlice = shortestPath(g, start.getLng(), start.getLat(), originalEnd.getLng(), originalEnd.getLat());
        start = originalStart;
        end = originalEnd;
        for (Long l : pathSlice)
            route.addLast(l);
        return route;
    }

    /**
     * Clear the route
     */
    public static void clearRoute() {
        start = null;
        end = null;
        stops = new ArrayList<>();
    }
}
