package com.mass.audio;

import net.wyun.audio.domain.Audio;
import net.wyun.audio.domain.AudioPayload;
import net.wyun.audio.rest.AudioReader;

import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class restUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void audioReadTest() throws IOException {

        String s = "test";
        byte[] audio = s.getBytes();

        AudioPayload payload = new AudioPayload("audio", Base64.encode(audio));
        AudioReader ar = new AudioReader("http://localhost:8080/");

        Map<String, String> map = ar.readAudio(payload);
        System.out.print(map);
    }


}