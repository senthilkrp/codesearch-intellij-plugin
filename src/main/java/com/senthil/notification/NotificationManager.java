package com.senthil.notification;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AppUIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Class to show notification to users
 */
public class NotificationManager {

  @NotNull
  private final Project myProject;

  @NotNull
  public static NotificationManager getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, NotificationManager.class);
  }

  public NotificationManager(@NotNull Project project) {
    myProject = project;
  }

  public void showNotification(@NotNull NotificationGroup group, @NotNull final String title,
      @NotNull final String message, @NotNull final NotificationType type,
      @Nullable final NotificationListener listener) {
    AppUIUtil.invokeLaterIfProjectAlive(myProject,
        () -> group.createNotification(title, message, type, listener).setIcon(group.getIcon()).notify(myProject));
  }
}
