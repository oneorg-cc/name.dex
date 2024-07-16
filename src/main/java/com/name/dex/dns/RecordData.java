package com.name.dex.dns;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.name.dex.utils.HashKey;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface RecordData {

    default String toContent() throws JsonProcessingException {
        StringBuilder contentBuilder = new StringBuilder();

        String serializedData = new ObjectMapper().writeValueAsString(this);
        JsonObject jsonData = new Gson().fromJson(serializedData, JsonObject.class);

        for(Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            contentBuilder.append(" " + entry.getValue().getAsString());
        }

        return contentBuilder.substring(1);
    }

    //

    class Domain extends HashKey {

        public static class InvalidFormatException extends Exception {

            public InvalidFormatException() { super(); }
            public InvalidFormatException(String message) { super(message); }

        }

        //

        public static final Pattern FORMAT_PATTERN = Pattern.compile("([a-z 0-9 áàâäãåæçéèêëíìîïñóòôöõøœßúùûüðýþÿ \\-_]+\\.)|(^(\\.)$)");

        public static boolean checkFormat(String name) {
            return name.length() >= 1 && name.endsWith(".") && FORMAT_PATTERN.matcher(name).find();
        }

        //

        private final String formatted;
        
        //
        
        public Domain(String name) throws InvalidFormatException {
            String formatted = name;
            formatted = formatted.toLowerCase();
            formatted = formatted.replaceAll("\\s", "-");
            formatted = formatted.endsWith(".") ? formatted : formatted + ".";

            if(!checkFormat(formatted))
                throw new InvalidFormatException("Invalid format for name `" + name + "` (formatted into `" + formatted + "`).");

            this.formatted = formatted;

            this.setRelevantObjectsSupplier(() -> List.of(this.toString()));
        }
        
        //

        public Domain shifted() throws InvalidFormatException {
            String str = this.toString();

            int dotIndex = str.indexOf(".");
            return dotIndex < 0 ? new Domain(".") : new Domain(str.substring(dotIndex+1));
        }

        public Domain popped() throws InvalidFormatException {
            String str = this.toString();
            str = str.substring(0, str.length()-1);

            int dotIndex = str.lastIndexOf(".");
            return dotIndex < 0 ? new Domain(".") : new Domain(str.substring(0, dotIndex) + ".");
        }

        //

        @Override
        @JsonValue
        public String toString() {
            return this.formatted;
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }

    //

}
