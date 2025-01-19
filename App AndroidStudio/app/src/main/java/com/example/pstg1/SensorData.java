package com.example.pstg1;

public class SensorData {
    private float data;
    private String sensedAt;
    private String type;

    // Getters y setters
    public float getData() {
        return data;
    }

    public void setData(float data) {
        this.data = data;
    }

    public String getSensedAt() {
        return sensedAt;
    }

    public void setSensedAt(String sensedAt) {
        this.sensedAt = sensedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
