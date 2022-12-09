import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
/**
 * Demonstrates the calculation of shortest paths in the US Highway
 * network, showing the functionality of GraphProcessor and using
 * Visualize
 */
public class GraphDemo {
    static Map<String, Point> cities = new HashMap<>();
    public static void main(String[] args) throws Exception {
        GraphProcessor distCalcu = new GraphProcessor();
        distCalcu.initialize(new FileInputStream("data/usa.graph"));

        Scanner reader = new Scanner(new File("data/uscities.csv"));

        reader.nextLine();
        
        while (reader.hasNext()){
            String[] line = reader.nextLine().split(",");
            Point lat_lon = new Point(Double.parseDouble(line[2]), Double.parseDouble(line[3]));
            cities.putIfAbsent(line[0] + " " + line[1], lat_lon);  
        }
        reader.close();

        Scanner sc = new Scanner(System.in);   
        System.out.println("Where are you starting from?");  
        String city1 = sc.nextLine();

        System.out.println("Where are you going?");  
        String city2 = sc.nextLine();
        sc.close();

        long startTime = System.nanoTime();
        Point startCity = cities.get(city1);
        Point endCity = cities.get(city2);

        Point start = distCalcu.nearestPoint(startCity);
        Point end = distCalcu.nearestPoint(endCity);

        List<Point> route = distCalcu.route(start, end);
        double distance = distCalcu.routeDistance(route);
        long endTime = System.nanoTime();

        System.out.printf("Nearest point to %s is %s\n", city1, start.toString());
        System.out.printf("Nearest point to %s is %s\n", city2, end.toString());
        System.out.printf("Route between %s and %s is %f total miles\n", start.toString(), end.toString(), distance);
        System.out.printf("Total time to get nearest points, route, and get distance: %f ms\n", (endTime - startTime)/1e6);
        
        Visualize path = new Visualize("data/usa.vis", "images/usa.png");
        path.drawPoint(start);
        path.drawPoint(end);
        path.drawRoute(route);
    }
}