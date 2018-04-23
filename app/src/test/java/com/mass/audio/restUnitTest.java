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
/*
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }*/

    @Test
    public void audioReadTest() throws IOException {

        String s = "中文测试";
        byte[] audio = s.getBytes();
        System.out.print(s);

        AudioPayload payload = new AudioPayload("audio", Base64.encode(audio));
        //AudioPayload payload = new AudioPayload("audio", "test");
        AudioReader ar = new AudioReader("http://localhost:8080/");

        Map<String, String> map = ar.readAudio(payload);
        System.out.print(map);
        System.out.println("通过Map.keySet遍历key和value：");
        for (String key : map.keySet()) {
            System.out.println("key= "+ key + " and value= " + map.get(key));
        }

    }


}