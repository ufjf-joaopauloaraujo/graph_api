package examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
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

    private long delay = 0; // in ms

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

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void waitForDelay() {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
                // ignoring
            }
        }
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

    public Long createVertex(String name) {
        return createVertex(name, VertexColor.WHITE);
    }

    public Long createVertex(String name, VertexColor color) {
        waitForDelay();

        try {
            URL apiUrl = new URL(VERTEX_API_URL);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format("{ \"name\": \"%s\", \"color\": \"%s\" }", name, color);

            // Write request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
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

    public void updateVertex(long id, String name, VertexColor color) {
        waitForDelay();

        try {
            StringBuilder body = new StringBuilder();
            body.append("{");
            if (null != name) {
                body.append(String.format("\"name\": \"%s\"", name));
                if (null != color) {
                    body.append(", ");
                }
            }
            if (null != color) {
                body.append(String.format("\"color\": \"%s\"", color));
            }
            body.append("}");

            String requestBody = body.toString();

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VERTEX_API_URL + id + "/"))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteVertex(long id) {
        waitForDelay();

        try {
            URL apiUrl = new URL(VERTEX_API_URL + id + "/");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);

            // Write request body
            try (OutputStream os = connection.getOutputStream()) {
                os.write(new byte[0], 0, 0);
            }

            // Read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
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

    public Long createEdge(long sourceId, long targetId) {
        return createEdge(sourceId, targetId, "");
    }

    public Long createEdge(long sourceId, long targetId, String description) {
        waitForDelay();

        try {
            URL apiUrl = new URL(EDGE_API_URL);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format(
                "{ \"source\": %d, \"target\": %d, \"description\": \"%s\" }",
                sourceId, targetId, description);

            // Write request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
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

    public void updateEdge(long id, Long sourceId, Long targetId, String description) {
        waitForDelay();

        try {
            StringBuilder body = new StringBuilder();
            body.append("{");
            if (null != sourceId) {
                body.append(String.format("\"source\": %d", sourceId));
                if (null != targetId || null != description) {
                    body.append(", ");
                }
            }
            if (null != targetId) {
                body.append(String.format("\"target\": %d", targetId));
                if (null != description) {
                    body.append(", ");
                }
            }
            if (null != description) {
                body.append(String.format("\"description\": \"%s\"", description));
            }
            body.append("}");

            String requestBody = body.toString();

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EDGE_API_URL + id + "/"))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteEdge(long id) {
        waitForDelay();

        try {
            URL apiUrl = new URL(EDGE_API_URL + id + "/");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);

            // Write request body
            try (OutputStream os = connection.getOutputStream()) {
                os.write(new byte[0], 0, 0);
            }

            // Read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
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

    public void resetModel() {
        waitForDelay();
        
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
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
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
