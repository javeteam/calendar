package com.aspect.calendar.entity;

import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class AppConfig {
    public final FileAttribute<Set<PosixFilePermission>> UNIX_CHMOD_755 = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"));
    public final FileAttribute<Set<PosixFilePermission>> UNIX_CHMOD_775 = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxr-x"));
    public final Path projectsRoot;
    public final Path pmRoot;
    public final Path projectTemplate;
    public final String bitrixRESTUrl;
    public final int bitrixNotificationChatId;

    public AppConfig(Path projectsRoot, Path pmRoot, String bitrixRESTUrl, int bitrixNotificationChatId) {
        this.projectsRoot = projectsRoot;
        this.pmRoot = pmRoot;
        this.projectTemplate = projectsRoot.resolve("template");
        this.bitrixRESTUrl = bitrixRESTUrl;
        this.bitrixNotificationChatId = bitrixNotificationChatId;
    }
}
