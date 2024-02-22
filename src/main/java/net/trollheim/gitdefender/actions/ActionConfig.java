package net.trollheim.gitdefender.actions;

public interface ActionConfig{


    String get(String key);

    void put(String key, String value);
}
