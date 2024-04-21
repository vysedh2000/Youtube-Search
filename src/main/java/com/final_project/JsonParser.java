package com.final_project;

import java.lang.reflect.Type;
import com.google.gson.Gson;

public class JsonParser<T> {
    public T parse(String data, Type type) {
        Gson g = new Gson();
        return g.fromJson(data, type);

    }
}