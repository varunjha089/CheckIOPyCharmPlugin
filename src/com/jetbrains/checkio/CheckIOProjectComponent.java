package com.jetbrains.checkio;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.checkio.actions.CheckIOCheckSolutionAction;
import com.jetbrains.checkio.actions.CheckIORefreshFileAction;
import com.jetbrains.checkio.actions.CheckIOShowHintAction;
import com.jetbrains.checkio.actions.CheckIOUpdateProjectAction;
import com.jetbrains.checkio.ui.CheckIOToolWindow;
import com.jetbrains.edu.courseFormat.Course;
import com.jetbrains.edu.learning.StudyTaskManager;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CheckIOProjectComponent implements ProjectComponent {
  private final Project myProject;
  private static Map<String, String> myDeletedShortcuts = new HashMap<>();

  private CheckIOProjectComponent(Project project) {
    myProject = project;
  }



  @Override
  public void projectOpened() {
    Platform.setImplicitExit(false);
    StartupManager.getInstance(myProject).runWhenProjectIsInitialized(() -> {
      final Course course = StudyTaskManager.getInstance(myProject).getCourse();
      registerTaskToolWindow(course);
      registerShortcuts(course);
      CheckIOUtils.selectCurrentTask(myProject);
    });
  }

  public void registerTaskToolWindow(@Nullable final Course course) {
    if (course != null && course.getCourseType().equals(CheckIOUtils.COURSE_TYPE)) {
      registerToolWindowIfNeeded(CheckIOToolWindow.ID, ToolWindowAnchor.RIGHT);
      final ToolWindow toolWindow = getToolWindowByID(CheckIOToolWindow.ID);
      if (toolWindow != null) {
        CheckIOUtils.updateTaskToolWindow(myProject);
        toolWindow.show(null);
      }
    }
  }

  private void registerToolWindowIfNeeded(@NotNull final String id, @NotNull final ToolWindowAnchor anchor) {
    final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
    final ToolWindow toolWindow = toolWindowManager.getToolWindow(id);
    if (toolWindow == null) {
      toolWindowManager.registerToolWindow(id, true, anchor, myProject, true);
    }
  }

  private ToolWindow getToolWindowByID(@NotNull final String id) {
    final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
    return toolWindowManager.getToolWindow(id);
  }

  private static void registerShortcuts(@Nullable final Course course) {
    if (course != null && course.getCourseType().equals(CheckIOUtils.COURSE_TYPE)) {
      addShortcut(CheckIOCheckSolutionAction.SHORTCUT, CheckIOCheckSolutionAction.ACTION_ID, false);
      addShortcut(CheckIOUpdateProjectAction.SHORTCUT, CheckIOUpdateProjectAction.ACTION_ID, false);
      addShortcut(CheckIORefreshFileAction.SHORTCUT, CheckIORefreshFileAction.ACTION_ID, false);
      addShortcut(CheckIOShowHintAction.SHORTCUT, CheckIOShowHintAction.ACTION_ID, false);
    }
  }

  private static void addShortcut(@NotNull final String shortcutString, @NotNull final String actionIdString, boolean isAdditional) {
    Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
    Shortcut[] shortcuts = keymap.getShortcuts(actionIdString);
    if (shortcuts.length > 0 && !isAdditional) {
      return;
    }
    Shortcut studyActionShortcut = new KeyboardShortcut(KeyStroke.getKeyStroke(shortcutString), null);
    String[] actionsIds = keymap.getActionIds(studyActionShortcut);
    for (String actionId : actionsIds) {
      myDeletedShortcuts.put(actionId, shortcutString);
      keymap.removeShortcut(actionId, studyActionShortcut);
    }
    keymap.addShortcut(actionIdString, studyActionShortcut);
  }

  public static CheckIOProjectComponent getInstance(@NotNull final Project project) {
    final Module module = ModuleManager.getInstance(project).getModules()[0];
    return module.getComponent(CheckIOProjectComponent.class);
  }

  @Override
  public void projectClosed() {
    final Course course = StudyTaskManager.getInstance(myProject).getCourse();
    if (course != null) {
      if (!myDeletedShortcuts.isEmpty()) {
        for (Map.Entry<String, String> shortcut : myDeletedShortcuts.entrySet()) {
          final Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
          final Shortcut actionShortcut = new KeyboardShortcut(KeyStroke.getKeyStroke(shortcut.getValue()), null);
          keymap.addShortcut(shortcut.getKey(), actionShortcut);
        }
      }
      FileUtil.delete(new File(myProject.getBasePath() + CheckIOUtils.PUBLICATION_FOLDER_NAME));
    }
  }

  @Override
  public void initComponent() {

  }

  @Override
  public void disposeComponent() {

  }

  @NotNull
  @Override
  public String getComponentName() {
    return "CheckIO Project Component";
  }
}
