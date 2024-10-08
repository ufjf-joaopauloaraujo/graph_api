import java.util.*;
import examples.GraphAPI;

class Graph {

    class Node { // Nó

        // API
        long apiId;
        String value;

        public Node(String value) {
            this.value = value;
            apiId = GraphAPI.getInstance().createVertex(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    class Edge { // Aresta

        // API
        long apiId;
        Node source;
        Node destination;
        String label;

        public Edge(Node source, Node destination) {
            this.source = source;
            this.destination = destination;
            this.label = "";
            apiId = GraphAPI.getInstance().createEdge(source.apiId, destination.apiId);
        }

        public Edge(Node source, Node destination, String label) {
            this.source = source;
            this.destination = destination;
            this.label = label;
            apiId = GraphAPI.getInstance().createEdge(source.apiId, destination.apiId, label);
        }
    }

    List<Node> nodes;
    List<Edge> edges;

    public Graph() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
    }

    Node addVertex(String name) {
        Node n = new Node(name);
        nodes.add(n);
        return n;
    }

    Edge addEdge(Node source, Node destination) {
        return addEdge(source, destination, null);
    }

    Edge addEdge(Node source, Node destination, String label) {
        Edge e = null == label ? new Edge(source, destination) : new Edge(source, destination, label);
        edges.add(e);
        return e;
    }

    // Print the adjacency list
    public void printGraph() {
        System.out.println("Nodes: ");
        for (Node n : nodes) {
            System.out.print(n + " ");
        }
        System.out.println();
        System.out.println("**********");

        System.out.println("Edges: ");
        for (Edge e : edges) {
            System.out.println(e.source + "-" + e.label + "-> " + e.destination);
        }
    }

    public static void main(String[] args) {
        GraphAPI.getInstance().resetModel();
        Graph graph = new Graph();
        Node n1 = graph.addVertex("1");
        Node n2 = graph.addVertex("2");
        Node n3 = graph.addVertex("3");
        Node n4 = graph.addVertex("4");

        Edge e1 = graph.addEdge(n1, n2);
        Edge e2 = graph.addEdge(n2, n3);
        Edge e3 = graph.addEdge(n3, n4);
        Edge e4 = graph.addEdge(n4, n1);

        GraphAPI.getInstance().updateVertex(n1.apiId, null, GraphAPI.VertexColor.RED);
        GraphAPI.getInstance().updateVertex(n2.apiId, "5", GraphAPI.VertexColor.BLUE);
        GraphAPI.getInstance().updateVertex(n3.apiId, null, GraphAPI.VertexColor.YELLOW);
        GraphAPI.getInstance().updateVertex(n4.apiId, "6", null);

        GraphAPI.getInstance().updateEdge(e1.apiId, null, null, "a");
        GraphAPI.getInstance().updateEdge(e2.apiId, null, n4.apiId, "b");
        GraphAPI.getInstance().updateEdge(e3.apiId, n1.apiId, null, "c");
        GraphAPI.getInstance().updateEdge(e4.apiId, n1.apiId, n3.apiId, "d");

        GraphAPI.getInstance().deleteVertex(n2.apiId);
        GraphAPI.getInstance().deleteEdge(e3.apiId);

        graph.printGraph();
    }
}
