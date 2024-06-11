import java.util.*;
import examples.GraphAPI;

class Graph {
    private Map<Integer, List<Integer>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    // Add a vertex (node) to the graph
    public void addVertex(int vertex) {
        adjacencyList.put(vertex, new ArrayList<>());
        GraphAPI.getInstance().postVertex(vertex);
    }

    // Add an edge between two vertices
    public void addEdge(int source, int destination) {
        adjacencyList.get(source).add(destination);
        adjacencyList.get(destination).add(source); // For an undirected graph
        GraphAPI.getInstance().postEdge(source, destination);
    }

    // Print the adjacency list
    public void printGraph() {
        for (Map.Entry<Integer, List<Integer>> entry : adjacencyList.entrySet()) {
            System.out.print("Vertex " + entry.getKey() + " is connected to: ");
            for (int neighbor : entry.getValue()) {
                System.out.print(neighbor + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Graph graph = new Graph();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);

        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 1);

        graph.printGraph();
    }
}
