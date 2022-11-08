import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;
import java.io.IOException;

public class StravaApi {

    static HttpClient client = HttpClient.newHttpClient();

    public static void initClient() {

        try {

            final String bearer = System.getenv("BEARER");
            if (bearer == null) {
                System.out.println("Bearer token not set");
                System.exit(1);
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://www.strava.com/api/v3/athlete"))
                .header("Content-type", "application/json")
                .setHeader("Authorization", "Bearer " + bearer)
                .build();
                    
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            System.out.println(response.statusCode());
            System.out.println(response.body());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    
}