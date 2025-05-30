package org.univaq.swa.eventsrest.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

public class EventsREST_Client {

    private static final String baseURI = "http://localhost:8080/EventsREST/rest";

    //una entry di esempio, già serializzata in JSON (come farebbe Google Gson, per esempio)  
    private static final String dummy_json_entry = "{ \"uid\" : \"IDabc\", \"summary\" : \"Event IDabc\", \"location\" : null, \"start\" : \"2024-04-10T13:48:48+02:00\", \"end\" : \"2024-04-10T13:48:48.295207+02:00\", \"categories\" : null, \"attachment\" : \"Y2lhbyBhIHR1dHRp\", \"participants\" : [ { \"name\" : \"Pinco Pallino #0\", \"email\" : \"pinco.pallino0@univaq.it\" }, { \"name\" : \"Pinco Pallino #1\", \"email\" : \"pinco.pallino1@univaq.it\" } ], \"recurrence\" : { \"count\" : null, \"interval\" : 2, \"until\" : \"2024-06-10T13:48+02:00\", \"frequency\" : \"WEEKLY\" } }";
    //la struttura usata per passere le credenziali all'endpoint login2
    private static final String dummy_json_credentials = "{ \"username\" : \"pippo\", \"password\" : \"pippopass\" }";

    //usiamo Apache Httpclient perchè molto più intuitivo della classi Java.net...
    CloseableHttpClient client = HttpClients.createDefault();

    private void logRequest(ClassicHttpRequest request) {
        try {
            System.out.println("* Metodo: " + request.getMethod());
            System.out.println("* URL: " + request.getRequestUri());
            if (request.getFirstHeader("Accept") != null) {
                System.out.println("* " + request.getFirstHeader("Accept"));
            }
            System.out.println("* Headers: ");
            Header[] request_headers = request.getHeaders();
            for (Header header : request_headers) {
                System.out.println("** " + header.getName() + " = " + header.getValue());
            }
            switch (request.getMethod()) {
                case "POST" -> {
                    HttpEntity e = ((HttpPost) request).getEntity();
                    System.out.print("* Payload: ");
                    e.writeTo(System.out);
                    System.out.println();
                    System.out.println("* Tipo payload: " + e.getContentType());
                }
                case "PUT" -> {
                    HttpEntity e = ((HttpPut) request).getEntity();
                    System.out.print("* Payload: ");
                    e.writeTo(System.out);
                    System.out.println();
                    System.out.println("* Tipo payload: " + e.getContentType());
                }
                case "PATCH" -> {
                    HttpEntity e = ((HttpPatch) request).getEntity();
                    System.out.print("* Payload: ");
                    e.writeTo(System.out);
                    System.out.println();
                    System.out.println("* Tipo payload: " + e.getContentType());
                }
                default -> {
                }
            }
        } catch (IOException ex) {
            System.out.println("Cannot dump request: " + ex.getMessage());
        }
    }

    private void logResponse(ClassicHttpResponse response) {
        System.out.println("* Headers: ");
        Header[] response_headers = response.getHeaders();
        for (Header header : response_headers) {
            System.out.println("** " + header.getName() + " = " + header.getValue());
        }
        System.out.println("* Return status: " + response.getReasonPhrase() + " (" + response.getCode() + ")");
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                entity.writeTo(System.out);
                System.out.println();
            } catch (IOException ex) {
                System.out.println("Cannot dump response: " + ex.getMessage());
            }
        }
    }

    private void executeAndDump(String description, ClassicHttpRequest request) {

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(description);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("REQUEST: ");
        logRequest(request);
        try {
            client.execute(request, response -> {
                //preleviamo il contenuto della risposta
                System.out.println("RESPONSE: ");
                logResponse(response);
                return null;
            });
        } catch (IOException ex) {
            System.out.println("Cannot execute request: " + ex.getMessage());
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println();

    }

    public void doTests() throws IOException {

        //GET rest/events?from=2024-02-01T00:00Z&to=2024-03-01T00:00Z&cat=work,personal
        //creiamo la richiesta (GET)
        HttpGet get_request = new HttpGet(baseURI + "/events?from=2024-02-01T00:00Z&to=2024-03-01T00:00Z&cat=work,personal");
        get_request.setHeader("Accept", "application/json");
        executeAndDump("Lista collection", get_request);

        get_request = new HttpGet(baseURI + "/events/count?from=2024-02-01T00:00Z&to=2024-03-01T00:00Z&cat=work,personal");
        get_request.setHeader("Accept", "application/json");
        executeAndDump("Dimensione collection", get_request);

        get_request = new HttpGet(baseURI + "/events/IDabc");
        get_request.setHeader("Accept", "application/json");
        executeAndDump("Singolo item", get_request);

        get_request = new HttpGet(baseURI + "/events/IDabc/participants");
        get_request.setHeader("Accept", "application/json");
        executeAndDump("Sotto-item", get_request);

        HttpPost post_request = new HttpPost(baseURI + "/auth/login");
        post_request.setEntity(new StringEntity(dummy_json_credentials, ContentType.APPLICATION_JSON));
        executeAndDump("Login (con oggetto credentials)", post_request);

        //ripetiamo la request per catturare il token...
        Header ah = client.execute(post_request, response -> {
            return response.getFirstHeader("Authorization");
        });

        post_request = new HttpPost(baseURI + "/auth/login2");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", "pippo"));
        params.add(new BasicNameValuePair("password", "pippopass"));
        post_request.setEntity(new UrlEncodedFormEntity(params));        
        executeAndDump("Login (con form parameters)", post_request);

        post_request = new HttpPost(baseURI + "/events");
        //per una richiesta POST, prepariamo anche il payload specificandone il tipo
        HttpEntity payload = new StringEntity(dummy_json_entry, ContentType.APPLICATION_JSON);
        //e lo inseriamo nella richiesta
        post_request.setEntity(payload);
        post_request.setHeader("Authorization", ah.getValue());
        executeAndDump("Creazione item", post_request);

        HttpPut put_request = new HttpPut(baseURI + "/events/IDabc");
        //per una richiesta PUT, prepariamo anche il payload specificandone il tipo
        payload = new StringEntity(dummy_json_entry, ContentType.APPLICATION_JSON);
        //e lo inseriamo nella richiesta
        put_request.setEntity(payload);
        put_request.setHeader("Authorization", ah.getValue());
        executeAndDump("Aggiornamento item", put_request);

        HttpDelete delete_request = new HttpDelete(baseURI + "/events/IDabc");
        delete_request.setHeader("Authorization", ah.getValue());
        executeAndDump("Eliminazione item", delete_request);

        //proviamo senza autenticazione...
        delete_request.removeHeaders("Authorization");
        executeAndDump("Eliminazione item (senza autorizzazione)", delete_request);

    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        EventsREST_Client instance = new EventsREST_Client();
        instance.doTests();
    }
}
