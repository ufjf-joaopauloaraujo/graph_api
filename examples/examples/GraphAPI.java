package examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphAPI {
    public static final String API_URL = "http://localhost:8000/api/"; // TODO: to be generated by application on download
    public static final String VERTEX_API_URL = API_URL + "vertices/";
    public static final String EDGE_API_URL = API_URL + "edges/";
    public static final String TOKEN = ""; // TODO
    public static final Pattern idPattern = Pattern.compile("\"id\":(\\d+)");

    private static GraphAPI instance;
    private final Lock lock = new ReentrantLock(); // For synchronization

    // Private constructor to prevent direct instantiation
    private GraphAPI() {
    }

    // Singleton instance creation
    public static GraphAPI getInstance() {
        if (instance == null) {
            synchronized (GraphAPI.class) {
                if (instance == null) {
                    instance = new GraphAPI();
                }
            }
        }
        return instance;
    }

    static long getIdFromResponse(String response) {
        Pattern idPattern = Pattern.compile("\"id\":(\\d+)");
        Matcher m = idPattern.matcher(response);
        m.find();
        return Long.parseLong(m.group(1));
    }

    // Basic REST methods (you can customize these)
    public String get() {
        // Implement your GET logic here
        return "GET response for " + API_URL;
    }

    public Long postVertex(String name) {
        return postVertex(name, VertexColor.WHITE);
    }

    public Long postVertex(String name, VertexColor color) {
        try {
            URL apiUrl = new URL(VERTEX_API_URL);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format("{ \"name\": \"%s\", \"color\": \"%s\" }", name, color);

            // Write request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return getIdFromResponse(response.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long postEdge(Long source, Long target) {
        return postEdge(source, target, "");
    }

    public Long postEdge(Long source, Long target, String description) {
        try {
            URL apiUrl = new URL(EDGE_API_URL);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format(
                "{ \"source\": %d, \"target\": %d, \"description\": \"%s\" }",
                source, target, description); // TODO: how to deal with IDs

            // Write request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return getIdFromResponse(response.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Other methods for handling REST requests go here
    // ...

    public void resetModel() {
        try {
            URL apiUrl = new URL(VERTEX_API_URL + "all/");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);

            // Write request body
            try (OutputStream os = connection.getOutputStream()) {
                os.write(new byte[0], 0, 0);
            }

            // Read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum VertexColor {
        RED("red"),
        YELLOW("yellow"),
        BLUE("blue"),
        ORANGE("orange"),
        GREEN("green"),
        PURPLE("purple"),
        PINK("pink"),
        BROWN("brown"),
        GREY("grey"),
        CYAN("cyan"),
        WHITE("white"),
        BEIGE("beige");
    
        private final String name;
    
        private VertexColor(String s) {
            name = s;
        }
    
        @Override
        public String toString() {
            return name;
        }
    }
}
