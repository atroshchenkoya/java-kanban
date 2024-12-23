package ru.practicum.kanban.servers.handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime dateTime) throws IOException {
        if (dateTime == null) {
            out.nullValue();
            return;
        }
        out.value(dateTime.format(formatter));
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        String dateTimeString = in.nextString();
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, formatter);
    }
}