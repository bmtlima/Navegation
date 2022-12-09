import java.security.InvalidAlgorithmParameterException;
import java.util.*;
import java.io.FileInputStream;

/**
 * Models a weighted graph of latitude-longitude points
 * and supports various distance and routing operations.
 * To do: Add your name(s) as additional authors
 */
public class GraphProcessor {
    private Map<String, Point> labels    = new HashMap<>();    // map to store name/label with the associated points (longitude and latitude)
    private Map<Point, Set<Point>> aList = new HashMap<>();    // adjacency list for graph, vertices are point --> Lookup time O(V)

    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * @param file a FileInputStream of the .graph file
     * @throws Exception if file not found or error reading
     */
    public void initialize(FileInputStream file) throws Exception {
        Scanner reader = new Scanner(file); // make a scanner for the file

        int numVertices = 0;
        int numEdges    = 0;
        // read first line of graph file || throws an exception if incorrect
        if (reader.hasNextInt()){
            numVertices = reader.nextInt(); // first int of a graph file is the number of vertices
        } else {
            reader.close();                 // close reader before terminate
            throw new Exception(".graph file formatted incorrectly");
        }
        if (reader.hasNextInt()){
            numEdges    = reader.nextInt(); // second int of a graph file is the number of edges
        } else {
            reader.close();                 // close reader before terminate
            throw new Exception(".graph file formatted incorrectly");
        }
        reader.nextLine();                  // gotta move it to next line since the rest of them use nextLine

        Map<Integer, Point> index = new HashMap<>();                  // store there index in appearing order on the graph (to be used when creating edges)
        // The next num_vertices lines describe one vertex/node per line, giving its name/label, then its latitude, then its longitude, all space separated.
        for (int i = 0; i < numVertices; i++ ){
            String[] vertexInfo = reader.nextLine().split(" "); // read next line, split by spaces
            String name         = vertexInfo[0];                       // first thing on line is the name/label that is a String
            Double lat          = Double.parseDouble(vertexInfo[1]);   // next thing on line is the latitude which is a double
            Double lon          = Double.parseDouble(vertexInfo[2]);   // next thing on line is the longitude which is a double
            Point vertex        = new Point(lat,lon);

            labels.put(name, vertex);                                  // add name with associated point to labels
            aList.put(vertex, new HashSet<Point>());                   // add point with empty adjacency list
            index.put(i, vertex);                                      // just for the next step
        }

        // The next num_edges lines describe one edge per line, giving the index of its first endpoint and then the index of its second endpoint, space separated. 
        // These indices refer to the order in which the vertices/nodes appear in this file (0-indexed). 
        // For example, 0 1 would mean there is an edge between the first and second vertices listed above in the file.
        for (int i = 0; i < numEdges; i++) {
            String[] edges = reader.nextLine().split(" ");
            int beginning  = Integer.parseInt(edges[0]);            // start of pair connection
            int end        = Integer.parseInt(edges[1]);            // end of pair connection

            aList.get(index.get(beginning)).add(index.get(end));    // add the pair connection to the adjacency list
            aList.get(index.get(end)).add(index.get(beginning));    // add the pair connection to the adjacency list (undirect so both ways)
        }
        reader.close();
    }


    /**
     * Searches for the point in the graph that is closest in
     * straight-line distance to the parameter point p
     * @param p A point, not necessarily in the graph
     * @return The closest point in the graph to p
     */
    public Point nearestPoint(Point p) {
        double nearestDistance = -1;                        // default value since distance will never return -1
        Point nearestPoint     = null;                      // nearestPoint null

        for (Point comparison : aList.keySet()) {           // loop through all the other points and find the one with the nearest distance
            if (nearestDistance == -1) {                    // if it equals -1 it's the first distance
                nearestDistance = p.distance(comparison);   // so just set nearest to whatever stuff by the first point
                nearestPoint    = comparison;
                continue;
            }

            double temp = p.distance(comparison);           // otherwise find the distance
            if (temp < nearestDistance) {                   // if it is a smaller distance
                nearestDistance = temp;                     // set nearest distnace to new distance
                nearestPoint    = comparison;               // set nearest point equal to the point found in map
            }
        }
        
        return nearestPoint;
    }


    /**
     * Calculates the total distance along the route, summing
     * the distance between the first and the second Points, 
     * the second and the third, ..., the second to last and
     * the last. Distance returned in miles.
     * @param start Beginning point. May or may not be in the graph.
     * @param end Destination point May or may not be in the graph.
     * @return The distance to get from start to end
     */
    public double routeDistance(List<Point> route) {
        double totDistance = 0;

        for (int i = 0; i < route.size() - 1; i++) {                // for all point pairs
            totDistance += route.get(i).distance(route.get(i+1));                     // calculate the distance between the points and add
        }

        return totDistance;
    }
    

    /**
     * Checks if input points are part of a connected component
     * in the graph, that is, can one get from one to the other
     * only traversing edges in the graph
     * @param p1 one point
     * @param p2 another point
     * @return true if p2 is reachable from p1 (and vice versa)
     */
    public boolean connected(Point p1, Point p2) {
        // the method should return false, including if p1 or p2 are not themselves points in the graph.
        if (!aList.containsKey(p1) || !aList.containsKey(p2)) {
            return false;
        }

        // return true if the points are connected, meaning there exists a path in the graph (a sequence of edges) from p1 to p2
        return dfs(p1, p2);
    }

    /**
     * Helper method for depth first search algorithm
     * @param start starting point
     * @param end ending point
     * @return boolean if the points are connected
     */
    private boolean dfs(Point start, Point end){
        Set<Point> visited = new HashSet<>();
        Stack<Point> toExplore = new Stack<>();
        Point current = start;

        toExplore.add(current);
        while (!toExplore.isEmpty()) {
            current = toExplore.pop();
            for (Point neighbor : aList.get(current)) {
                if (neighbor.equals(end)){
                    return true;
                }

                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    toExplore.push(neighbor);
                }
            }
        }

        return false;
    }


    /**
     * Returns the shortest path, traversing the graph, that begins at start
     * and terminates at end, including start and end as the first and last
     * points in the returned list. If there is no such route, either because
     * start is not connected to end or because start equals end, throws an
     * exception.
     * @param start Beginning point.
     * @param end Destination point.
     * @return The shortest path [start, ..., end].
     * @throws InvalidAlgorithmParameterException if there is no such route, 
     * either because start is not connected to end or because start equals end.
     */
    public List<Point> route(Point start, Point end) throws InvalidAlgorithmParameterException {
        // If there is no path between start and end, either because the two points are not in the graph, 
        // or because they are the same point, or because they are not connected in the graph
        if (!connected(start, end) || start.equals(end)) {
            throw new InvalidAlgorithmParameterException("No path between start and end");
        } 

        List<Point> path = new LinkedList<>();
        Map<Point,Point> previous = new HashMap<>(dijkstra(start));
        // end -> previous.get(end) -> previous.get(previous.get(end)) -> ... -> start
        Point current = end;
        path.add(current);
        while (!current.equals(start)) {
            current = previous.get(current);
            path.add(0, current);
        }

        return path;
    }

    private Map<Point, Point> dijkstra(Point start) {
        Map<Point, Point> previous = new HashMap<>();
        
        Map<Point, Double> distance = new HashMap<>();
        Comparator<Point> comp = (a, b) -> (int)(distance.get(a) - distance.get(b));
        PriorityQueue<Point> toExplore = new PriorityQueue<>(comp);

        Point current = start;
        distance.put(current, 0.0);
        toExplore.add(current);

        while (!toExplore.isEmpty()){
            current = toExplore.remove();
            for (Point neighbor : aList.get(current)) {
                double weight = current.distance(neighbor);
                if (!distance.containsKey(neighbor) || distance.get(neighbor) > distance.get(current) + weight) {
                    distance.put(neighbor, distance.get(current) + weight);
                    previous.put(neighbor, current);
                    toExplore.add(neighbor);
                }
            }
        }

        return previous;
    }

    
}