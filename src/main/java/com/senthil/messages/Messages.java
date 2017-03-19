package com.senthil.messages;

import com.intellij.CommonBundle;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;


public class Messages {

  private static Reference<ResourceBundle> bundle;

  @NonNls
  private static final String BUNDLE = "messages.codesearch";

  private Messages() {
  }

  public static String message(@NotNull @NonNls @PropertyKey(resourceBundle = BUNDLE) final String key,
      @NotNull final Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(Messages.bundle);
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      Messages.bundle = new SoftReference<>(bundle);
    }
    return bundle;
  }
}
