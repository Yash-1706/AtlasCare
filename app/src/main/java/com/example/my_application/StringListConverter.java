package com.example.my_application;

import androidx.room.TypeConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StringListConverter {
    @TypeConverter
    public String fromList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(";;", list);
    }

    @TypeConverter
    public List<String> toList(String value) {
        if (value == null || value.isEmpty()) return Collections.emptyList();
        return Arrays.asList(value.split(";;"));
    }
}
