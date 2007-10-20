package fr.megiste.interloc.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

public class Messages {
    private static Messages messages;
    
    private Properties properties = new Properties();
    
    private Messages(){
        InputStream is = Messages.class.getClassLoader().getResourceAsStream("labels.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new InterlocException(e);
        }
    }
    
    public static Messages getInstance(){
        if(messages==null) messages = new Messages();
        return messages;
    }
    
    public String getValue(String key){
        String out = properties.getProperty(key);
        if(out==null){
            System.out.println(key + "=" + key);
            out = key;
        }
        return out;
    }

    public String getValue(String clef, Object[] values) {
        return new MessageFormat(getValue(clef)).format(values);
    }
    
    
}
