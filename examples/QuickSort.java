import java.util.ArrayList;
import java.util.List;

import examples.GraphAPI;
import examples.GraphAPI.VertexColor;

public class QuickSort {
  private static int partition(int[] arr, int low, int high) {
      int pivot = arr[high];

      // destacando pivô
      GraphAPI.getInstance().updateVertex(vIds.get(high), null, VertexColor.BLUE);

      int i = (low - 1);
      for (int j = low; j < high; j++) {
          if (arr[j] < pivot) {
              i++;
              int temp = arr[i];
              arr[i] = arr[j];
              arr[j] = temp;

              // atualizando e destacando vértices
              GraphAPI.getInstance().updateVertex(vIds.get(i), String.valueOf(arr[i]), VertexColor.ORANGE);
              GraphAPI.getInstance().updateVertex(vIds.get(j), String.valueOf(arr[j]), VertexColor.ORANGE);
          }
      }
      int temp = arr[i + 1];
      arr[i + 1] = arr[high];
      arr[high] = temp;

      // atualizando e destacando vértices
      GraphAPI.getInstance().updateVertex(vIds.get(i + 1), String.valueOf(arr[i + 1]), VertexColor.RED);
      GraphAPI.getInstance().updateVertex(vIds.get(high), String.valueOf(arr[high]), VertexColor.RED);
      return i + 1;
  }

  public static void quickSort(int[] arr, int low, int high) {
      if (low < high) {
          int pi = partition(arr, low, high);
          quickSort(arr, low, pi - 1);
          quickSort(arr, pi + 1, high);
      }
  }

  static List<Long> vIds = new ArrayList<>(); // lista de ids dos vértices
  static List<Long> eIds = new ArrayList<>(); // lista de ids das arestas

  public static void main(String[] args) {
      // reiniciando representação gráfica
      GraphAPI.getInstance().resetModel();

      int[] arr = {10, 7, 8, 9, 1, 5};
      int n = arr.length;

      // gerando vértices e armazenando ids
      for (int i : arr) {
        vIds.add(GraphAPI.getInstance().createVertex(String.valueOf(i)));
      }
      // gerando arestas e armazenando ids
      for (int i = 1; i < vIds.size(); i++) {
        eIds.add(GraphAPI.getInstance().createEdge(vIds.get(i-1), vIds.get(i)));
      }

      quickSort(arr, 0, n - 1);
      System.out.println("Sorted array: ");
      for (int num : arr) {
          System.out.print(num + " ");
      }
  }
}
