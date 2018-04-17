package net.wyun.audio.domain;

public class AudioPayload {

    private String type;

    public AudioPayload(String type, String encodedAudio) {
        this.type = type;
        this.encodedAudio = encodedAudio;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEncodedAudio() {
        return encodedAudio;
    }

    public void setEncodedAudio(String encodedAudio) {
        this.encodedAudio = encodedAudio;
    }

    private String encodedAudio;



}
