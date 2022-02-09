package com.fubigemo.real_volume;

public class VolumeObj {
    private int streamType;
    private double volumeLevel;

    public VolumeObj(int streamType, double volumeLevel) {
        this.streamType = streamType;
        this.volumeLevel = volumeLevel;
    }

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    public double getVolumeLevel() {
        return volumeLevel;
    }

    public void setVolumeLevel(double volumeLevel) {
        this.volumeLevel = volumeLevel;
    }
}
