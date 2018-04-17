package net.wyun.audio.rest;

import net.wyun.audio.domain.AudioPayload;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AudioApiService {

    @POST("/api/audio")
    Call<Map<String, String>> readAudio(@Body AudioPayload payload);
}
