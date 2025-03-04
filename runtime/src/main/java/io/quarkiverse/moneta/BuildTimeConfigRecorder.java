package io.quarkiverse.moneta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BuildTimeConfigRecorder {

    private static final Map<String, String> properties = new ConcurrentHashMap<>();

    public void set(String key, String value) {
        properties.put(key, value);
    }

    public static Map<String, String> getProperties() {
        return properties;
    }
}
