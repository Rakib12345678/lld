package com.rockydrive.notification.notify;

import com.rockydrive.common.event.FileEvent;

import java.util.Set;

/** Resolves which users should be notified for a given {@link FileEvent}. */
public interface AffectedUserResolver {

    Set<String> resolve(FileEvent event);
}
