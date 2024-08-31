import java.util.*;
import examples.GraphAPI;
import examples.GraphAPI.VertexColor;

// Classe que representa um grafo direcionado usando lista de adjacência
class Grafo {
    private int V; // Número de vértices
    private LinkedList<Integer> adj[]; // Lista de adjacência

    private long[] vIds; // lista de ids dos vértices
    // Construtor
    Grafo(int v) {
        V = v;
        adj = new LinkedList[v];
        vIds = new long[v];
        for (int i=0; i<v; ++i){
            adj[i] = new LinkedList<>();
            vIds[i] = GraphAPI.getInstance().createVertex(Integer.toString(i));
        }
    }

    // Função para adicionar uma aresta ao grafo
    void addAresta(int v, int w) {
        adj[v].add(w); // Adiciona w à lista de v
        GraphAPI.getInstance().createEdge(vIds[v], vIds[w]);
    }

    // Função para realizar a busca em profundidade a partir de um vértice v
    void DFSUtil(int v, boolean visitado[]) {
        // Marca o vértice atual como visitado e imprime
        visitado[v] = true;
        System.out.print(v + " ");
        GraphAPI.getInstance().updateVertex(vIds[v], null, VertexColor.GREEN);

        // Recurso para todos os vértices adjacentes ao vértice atual
        Iterator<Integer> it = adj[v].listIterator();
        while (it.hasNext()) {
            int n = it.next();
            if (!visitado[n])
                DFSUtil(n, visitado); // Chama recursivamente o DFS se o vértice não foi visitado
        }
    }

    // Função para realizar a busca em profundidade a partir de um vértice específico
    void DFS(int v) {
        // Marca todos os vértices como não visitados
        boolean visitado[] = new boolean[V];

        // Chama a função utilitária recursiva para imprimir a DFS a partir de um vértice
        DFSUtil(v, visitado);
    }

    // Função principal de BFS a partir de um vértice específico
    void BFS(int s) {
        // Marca todos os vértices como não visitados
        boolean visitado[] = new boolean[V];

        // Fila para BFS
        LinkedList<Integer> fila = new LinkedList<Integer>();

        // Marca o vértice atual como visitado e o adiciona à fila
        visitado[s] = true;
        GraphAPI.getInstance().updateVertex(vIds[s], null, VertexColor.GREEN);
        fila.add(s);

        while (fila.size() != 0) {
            // Remove da fila um vértice e imprime
            s = fila.poll();
            System.out.print(s + " ");

            // Pega todos os vértices adjacentes ao vértice removido da fila
            Iterator<Integer> it = adj[s].listIterator();
            while (it.hasNext()) {
                int n = it.next();
                // Se um vértice adjacente ainda não foi visitado, marca como visitado e o adiciona à fila
                if (!visitado[n]) {
                    visitado[n] = true;
                    GraphAPI.getInstance().updateVertex(vIds[n], null, VertexColor.GREEN);
                    fila.add(n);
                }
            }
        }
    }

    public static void main(String args[]) {
        GraphAPI.getInstance().resetModel();
        Grafo g = new Grafo(8);

        g.addAresta(0, 1);
        g.addAresta(0, 2);
        g.addAresta(1, 3);
        g.addAresta(1, 4);
        g.addAresta(2, 5);
        g.addAresta(2, 6);
        g.addAresta(4, 7);

        System.out.println("Busca em Profundidade (DFS) a partir do vértice 2:");
        g.DFS(0);
        // g.BFS(0);
    }
}
