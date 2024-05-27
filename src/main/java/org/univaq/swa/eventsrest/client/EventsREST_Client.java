package org.univaq.swa.eventsrest.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class EventsREST_Client {

    private static final String baseURI = "http://localhost:8085/EventsREST/rest";

    //una entry di esempio, già serializzata in JSON (come farebbe Google Gson, per esempio)  
    private static final String dummy_json_entry = "{ \"uid\" : \"IDabc\", \"summary\" : \"Event IDabc\", \"location\" : null, \"start\" : \"2024-04-10T13:48:48+02:00\", \"end\" : \"2024-04-10T13:48:48.295207+02:00\", \"categories\" : null, \"attachment\" : \"Y2lhbyBhIHR1dHRp\", \"participants\" : [ { \"name\" : \"Pinco Pallino #0\", \"email\" : \"pinco.pallino0@univaq.it\" }, { \"name\" : \"Pinco Pallino #1\", \"email\" : \"pinco.pallino1@univaq.it\" } ], \"recurrence\" : { \"count\" : null, \"interval\" : 2, \"until\" : \"2024-06-10T13:48+02:00\", \"frequency\" : \"WEEKLY\" } }";

    //usiamo Apache Httpclient perchè molto più intuitivo della classi Java.net...
    CloseableHttpClient client = HttpClients.createDefault();

    private void executeAndDump(String description, HttpRequestBase request) {
        try {
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(description);
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("REQUEST: ");
            System.out.println("* Metodo: " + request.getMethod());
            System.out.println("* URL: " + request.getURI());
            if (request.getFirstHeader("Accept") != null) {
                System.out.println("* " + request.getFirstHeader("Accept"));
            }
            System.out.println("* Headers: ");
            Header[] headers = request.getAllHeaders();
            for (Header header : headers) {
                System.out.println("** " + header.getName() + " = " + header.getValue());
            }
            switch (request.getMethod()) {
                case "POST": {
                    HttpEntity e = ((HttpPost) request).getEntity();
                    System.out.print("* Payload: ");
                    e.writeTo(System.out);
                    System.out.println();
                    System.out.println("* Tipo payload: " + e.getContentType());
                    break;
                }
                case "PUT": {
                    HttpEntity e = ((HttpPut) request).getEntity();
                    System.out.print("* Payload: ");
                    e.writeTo(System.out);
                    System.out.println();
                    System.out.println("* Tipo payload: " + e.getContentType());
                    break;
                }
                case "PATCH": {
                    HttpEntity e = ((HttpPatch) request).getEntity();
                    System.out.print("* Payload: ");
                    e.writeTo(System.out);
                    System.out.println();
                    System.out.println("* Tipo payload: " + e.getContentType());
                    break;
                }
                default:
                    break;
            }
            try (CloseableHttpResponse response = client.execute(request)) {
                //preleviamo il contenuto della risposta
                System.out.println("RESPONSE: ");
                System.out.println("* Headers: ");
                headers = response.getAllHeaders();
                for (Header header : headers) {
                    System.out.println("** " + header.getName() + " = " + header.getValue());
                }
                System.out.println("* Return status: " + response.getStatusLine().getReasonPhrase() + " (" + response.getStatusLine().getStatusCode() + ")");
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    entity.writeTo(System.out);
                    System.out.println();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println();
    }

    public void doTests() throws IOException {

        //GET rest/events?from=2024-02-01T00:00Z&to=2024-03-01T00:00Z&cat=work,personal
        //creiamo la richiesta (GET)
        HttpGet get_request = new HttpGet(baseURI + "/events?from=2024-02-01T00:00Z&to=2024-03-01T00:00Z&cat=work,personal");
        get_request.setHeader("Accept", "application/json");
        executeAndDump("Lista collection",get_request);

        get_request = new HttpGet(baseURI + "/events/count?from=2024-02-01T00:00Z&to=2024-03-01T00:00Z&cat=work,personal");
        get_request.setHeader("Accept", "application/json");
        executeAndDump("Dimensione collection",get_request);

        get_request = new HttpGet(baseURI + "/events/IDabc");
        get_request.setHeader("Accept", "application/json");
        executeAndDump("Singolo item",get_request);

        get_request = new HttpGet(baseURI + "/events/IDabc/participants");
        get_request.setHeader("Accept", "application/json");
        executeAndDump("Sotto-item",get_request);

        HttpPost post_request = new HttpPost(baseURI + "/auth/login");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", "pippo"));
        params.add(new BasicNameValuePair("password", "pippopass"));
        post_request.setEntity(new UrlEncodedFormEntity(params));
        executeAndDump("Login",post_request);
        //ripetiamo la request per catturare il token...
        Header ah;
        try (CloseableHttpResponse response = client.execute(post_request)) {
            ah = response.getFirstHeader("Authorization");
        }

        post_request = new HttpPost(baseURI + "/events");
        //per una richiesta POST, prepariamo anche il payload specificandone il tipo
        HttpEntity payload = new StringEntity(dummy_json_entry, ContentType.APPLICATION_JSON);
        //e lo inseriamo nella richiesta
        post_request.setEntity(payload);
        post_request.setHeader("Authorization", ah.getValue());
        executeAndDump("Creazione item",post_request);

        HttpPut put_request = new HttpPut(baseURI + "/events/IDabc");
        //per una richiesta PUT, prepariamo anche il payload specificandone il tipo
        payload = new StringEntity(dummy_json_entry, ContentType.APPLICATION_JSON);
        //e lo inseriamo nella richiesta
        put_request.setEntity(payload);
        put_request.setHeader("Authorization", ah.getValue());
        executeAndDump("Aggiornamento item",put_request);

        HttpDelete delete_request = new HttpDelete(baseURI + "/events/IDabc");
        delete_request.setHeader("Authorization", ah.getValue());
        executeAndDump("Eliminazione item",delete_request);

        //proviamo senza autenticazione...
        delete_request.removeHeaders("Authorization");
        executeAndDump("Eliminazione item (senza autorizzazione)",delete_request);

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
