package net.wyun.audio.domain;

import android.util.Base64;

public class Audio {

    public Audio(byte[] audio) {
        this.audio = audio;
    }

    public byte[] getAudio() {
        return audio;
    }

    public void setAudio(byte[] audio) {
        this.audio = audio;
    }

    byte[] audio;

    /**
     * get the audio in an encoded form suitable for http transfer.
     * @return string containing the encoded audio
     */
    public String getEncodedAudio() {
        return encodeBytes(this.audio);
    }

    private String encodeBytes(byte[] bytes) {

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private byte[] decodeString(String encoded) {
        return Base64.decode(encoded, Base64.DEFAULT);
    }


}
