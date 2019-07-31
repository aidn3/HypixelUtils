
package com.aidn5.hypixelutils.v1.tools.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ConfigStorageFactory {
  InputStream in;

  public static <T> T jsonFromJar(String path, ClassLoader classLoader) throws IOException {
    return jsonFromStream(classLoader.getResourceAsStream(path));
  }

  public static <T> T jsonFromStream(InputStream in) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = in.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }

    in.close();

    String json = result.toString("UTF-8");

    Type listType = new TypeToken<T>() {}.getType();
    Gson gson = new Gson();

    return gson.fromJson(json, listType);
  }

  public static <T> void objectToJsonFile(File file, T objectToSave) throws IOException {
    ObjectToJsonStream(new FileOutputStream(file), objectToSave);
  }

  public static <T> void ObjectToJsonStream(OutputStream os, T objectToSave) throws IOException {
    Gson gson = new Gson();
    Type listType = new TypeToken<T>() {}.getType();
    String json = gson.toJson(objectToSave, listType);
    os.close();
  }
}
