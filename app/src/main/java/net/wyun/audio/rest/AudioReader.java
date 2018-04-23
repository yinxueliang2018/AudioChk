package net.wyun.audio.rest;

import net.wyun.audio.domain.Audio;
import net.wyun.audio.domain.AudioPayload;
import net.wyun.audio.domain.ReadResponse;

import java.io.IOException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class AudioReader {

    private static final  String DEFAULT_HOST_PORT = "http://localhost:8080/";
    protected String hostUrl;

    /**
     * Constructor for ImageRecognizer instances
     * @param hostUrl - hostUrl for audio reader server
     * */
    public AudioReader(String hostUrl) {
        this.hostUrl = hostUrl;
    }


    public Map<String, String> readAudio(Audio audio) throws IOException {
        AudioPayload payload = new AudioPayload("audio", audio.getEncodedAudio());

        return readAudio(payload);
    }

    public Map<String, String> readAudio(AudioPayload payload) throws IOException {

        Call<Map<String, String>> call = RetrofitClient.getClient(hostUrl)
                .create(AudioApiService.class)
                .readAudio(payload);

        Response<Map<String, String>> resp = call.execute();
        return resp.body();
    }

}
