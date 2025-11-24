package com.memozy.memozy_back.domain.memory.constant;

public enum PermissionLevel {
    VIEWER, EDITOR, OWNER, NONE;

    public boolean canEdit() {
        return this == OWNER || this == EDITOR;
    }

    public boolean canView() {
        return !(this == NONE);
    }
}
