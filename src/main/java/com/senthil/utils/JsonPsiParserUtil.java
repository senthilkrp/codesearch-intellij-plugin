package com.senthil.utils;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonLiteral;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.Nullable;


public final class JsonPsiParserUtil {
  private JsonPsiParserUtil() {
  }

  /**
   * @param jsonFile to start search from
   * @param path segment path to search (["foo", "bar"])
   * @return value or null if not found
   */
  @Nullable
  public static JsonValue findProperty(final JsonFile jsonFile, final String... path) {
    return jsonFile == null ? null : findProperty(jsonFile.getTopLevelValue(), path);
  }

  /**
   * @param root to start search from
   * @param path segment path to search (["foo", "bar"])
   * @return value or null if not found
   */
  @Nullable
  public static JsonValue findProperty(final JsonValue root, final String... path) {
    return root == null ? null : findChildByPath(root, 0, path);
  }

  /**
   * @param jsonFile to start search from
   * @param path segment path to search (["foo", "bar"])
   * @return value or null if not found
   */
  @Nullable
  public static String findPropertyValue(final JsonFile jsonFile, final String... path) {
    return jsonFile == null ? null : findPropertyValue(jsonFile.getTopLevelValue(), path);
  }

  /**
   * @param root to start search from
   * @param path segment path to search (["foo", "bar"])
   * @return value or null if not found
   */
  @Nullable
  public static String findPropertyValue(final JsonValue root, final String... path) {
    return getJsonPropertyValue(findProperty(root, path));
  }

  public static String getJsonPropertyValue(final JsonValue value) {
    if (value == null) {
      return null;
    }
    if (value instanceof JsonArray) {
      List<JsonValue> valueList = ((JsonArray) value).getValueList();
      String[] arrayTextValue = new String[valueList.size()];
      int i = 0;
      for (JsonValue jsonValue : valueList) {
        arrayTextValue[i++] = jsonValue.getText();
      }
      return Arrays.toString(arrayTextValue);
    }
    return value instanceof JsonStringLiteral ? ((JsonStringLiteral) value).getValue() : value.getText();
  }

  /**
   *
   * @param parent root of the json tree to start search from
   * @param path path to find, array of segments
   * @return parent if path is null or empty, null if path was not found, value otherwise
   */
  public static JsonValue findChildByPath(final JsonValue parent, final int index, final String... path) {
    if (parent == null || path == null || path.length == 0 || index == path.length) {
      return parent;
    }

    if (parent instanceof JsonArray) {
      for (final JsonValue jsonValue : ((JsonArray) parent).getValueList()) {
        JsonValue childByPath = findChildByPath(jsonValue, index, path);
        if (childByPath != null) {
          return childByPath;
        }
      }
    } else if (parent instanceof JsonObject) {
      final JsonProperty property = ((JsonObject) parent).findProperty(path[index]);
      if (property == null) {
        return null;
      }
      return findChildByPath(property.getValue(), index + 1, path);
    } else if (parent instanceof JsonLiteral) {
      String asString =
          parent instanceof JsonStringLiteral ? ((JsonStringLiteral) parent).getValue() : parent.getText();

      if (path[index].equals(asString)) {
        return parent;
      }
    }

    return null;
  }
}
