package com.denizyamac.synctemplates.helper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelper {
    public static boolean isValidJson(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            System.out.println("Deniz: "+ jsonString);
            e.printStackTrace();
            return false;
        }
    }

    public static <T> T convertToObject(String from, Class<T> type) {
        if(isValidJson(from)){
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(from, type);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
