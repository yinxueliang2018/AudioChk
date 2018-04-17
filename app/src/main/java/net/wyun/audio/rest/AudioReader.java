package net.wyun.audio.rest;

import net.wyun.audio.domain.Audio;
import net.wyun.audio.domain.AudioPayload;
import net.wyun.audio.domain.ReadResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AudioReader {

    protected static final String ACCEPT = "accept";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CONTENT_TYPE = "content-type";

    public static final String API_PATH = "api/audio";
    private static final  String DEFAULT_HOST_PORT = "http://localhost:8080";

    protected String hostUrl;

    /**
     * Constructor for ImageRecognizer instances
     * @param hostUrl - hostUrl for audio reader server
     * */
    public AudioReader(String hostUrl) {
        this.hostUrl = DEFAULT_HOST_PORT;
    }



    public Map<String, String> readAudio(Audio audio){
        URI requestUri = createURI();
        Map<String, String> response = injectAudio(audio, requestUri);

        return response;
    }

    protected static final String URL_DELIM = "/";

    protected  URI createURI() {
        StringBuilder builder = new StringBuilder(this.hostUrl);
        builder.append(URL_DELIM).append(API_PATH);

        String requestString = builder.toString();
        URI uri = null;

        try {
            uri = new URI(requestString);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to validate host URI: " +  requestString);
        }

        return uri;
    }



    protected  Map<String, String> injectAudio(Audio audio, URI request) {

        RestTemplate template = new RestTemplate();

        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();

        converters.add(new MappingJackson2HttpMessageConverter());

        AudioPayload payload = new AudioPayload();
        payload.setEncodedAudio(audio.getEncodedAudio());
        payload.setType("audio");


        HttpHeaders headers = new HttpHeaders();

        headers.set(CONTENT_TYPE, APPLICATION_JSON);
        headers.set(ACCEPT, APPLICATION_JSON);


        HttpEntity<AudioPayload> entity = new HttpEntity<AudioPayload>(payload, headers);

        template.setMessageConverters(converters);


        ResponseEntity<ReadResponse> response = template.postForEntity(request, entity, ReadResponse.class);

        if (HttpStatus.OK != response.getStatusCode()) {

        }

        // hack for marshalling
        //ResponseContainer container = template.postForObject(request, entity, ResponseContainer.class);
        Map<String, String> valueMap = response.getBody().getValues();
        //Map<String, String> valueMap = container.getReadResponse().getValues();
        return valueMap;

    }

}
