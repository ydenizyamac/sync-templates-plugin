package com.denizyamac.synctemplates.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonHelper {
    public static boolean isValidJson(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            System.out.println("Deniz: " + jsonString);
            e.printStackTrace();
            return false;
        }
    }

    public static String toString(Object object) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static <T> T convertToObject(String from, Class<T> type) {
        if (isValidJson(from)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

            try {
                return mapper.readValue(from, type);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog("ERROR", e.getMessage()));
                return null;
            }
        }
        return null;
    }
}
