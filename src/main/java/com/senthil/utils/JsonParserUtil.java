package com.senthil.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Utility class for parsing and retrieving Json information from json files
 */
public final class JsonParserUtil {
  private static final Logger LOG = Logger.getInstance(JsonParserUtil.class);

  private JsonParserUtil() {
  }

  /**
   * @param file file to parse
   * @param path tree path to the element
   * @return json element value for path
   */
  @Nullable
  public static String findValue(@NotNull File file, final String... path) {
    return findValue(getRootElement(file), path);
  }

  /**
   *
   * @param file to parse
   * @return root element for provided file or null if file does not exist or cannot be parsed
   */
  @Nullable
  public static JsonElement getRootElement(@NotNull File file) {
    try (InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
      return getRootElement(fileReader);
    } catch (JsonParseException | IOException e) {
      LOG.debug("Error getting root element", e);
      return null;
    }
  }
  /**
   *
   * @param reader to parse
   * @return root element for provided reader or null if reader cannot be parsed
   */
  public static JsonElement getRootElement(Reader reader) {
    JsonParser parser = new JsonParser();
    return parser.parse(reader);
  }

  /**
   *
   * @param root to start search from or null
   * @param path to the element to search
   * @return json element value for the path provided starting from root,
   * null if root is null or elements does not exist
   */
  @Nullable
  public static String findValue(@Nullable JsonElement root, String... path) {
    JsonElement childByPath = findChildByPath(root, path);
    return childByPath == null ? null
        : (childByPath.isJsonPrimitive() ? childByPath.getAsString() : childByPath.toString());
  }

  /**
   * @param parent root of the json tree to start search from
   * @param path path to find, array of segments
   * @return parent if path is null or empty, null if path was not found, value otherwise
   */

  public static JsonElement findChildByPath(final JsonElement parent, final String... path) {
    return findChildByPath(parent, 0, path);
  }

  /**
   * @param parent root of the json tree to start search from
   * @param index of first element in the path
   * @param path path to find, array of segments
   * @return parent if path is null or empty, null if path was not found, value otherwise
   */

  public static JsonElement findChildByPath(final JsonElement parent, final int index, final String... path) {
    if (parent == null || path == null || path.length == 0 || index == path.length) {
      return parent;
    }

    if (parent.isJsonArray()) {
      for (JsonElement jsonValue : parent.getAsJsonArray()) {
        JsonElement childByPath = findChildByPath(jsonValue, index, path);
        if (childByPath != null) {
          return childByPath;
        }
      }
    } else if (parent.isJsonObject()) {
      final JsonElement property = parent.getAsJsonObject().get(path[index]);
      if (property == null) {
        return null;
      }
      return findChildByPath(property, index + 1, path);
    } else if (parent.isJsonPrimitive()) {
      String asString = parent.getAsString();

      if (path[index].equals(asString)) {
        return parent;
      }
    }

    return null;
  }
}
