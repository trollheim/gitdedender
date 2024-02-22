package net.trollheim.gitdefender.cmd;

import net.trollheim.gitdefender.actions.ActionConfig;

import java.util.Properties;

public class ActionConfigProperties implements ActionConfig {

    private final Properties properties;

    public ActionConfigProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String get(String key) {
        return properties.getProperty(key);
    }

    @Override
    public void put(String key, String value) {
        properties.put(key, value);
    }
}
