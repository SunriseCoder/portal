package app.enums;

public enum OperationTypes {
    ACCESS_ADMIN_AUDIT,
    ACCESS_ADMIN_DASHBOARD,
    ACCESS_ADMIN_LOGS,

    ACCESS_PAGE_FILES,
    ACCESS_PAGE_MAIN,
    ACCESS_PAGE_UPLOAD,

    ACCESS_ROLE_LIST,
    ACCESS_ROLE_CREATE,
    ACCESS_ROLE_EDIT,

    ACCESS_USER_LIST,
    ACCESS_USER_EDIT,
    ACCESS_USER_LOGIN,
    ACCESS_USER_LOGOUT,
    ACCESS_PAGE_ERROR,
    ACCESS_PAGE_STATIC,

    CHANGE_FILE_UPLOAD,

    CHANGE_ROLE_SAVE,
    CHANGE_ROLE_DELETE,

    CHANGE_USER_REGISTER,
    CHANGE_USER_LOGIN,
    CHANGE_USER_PASSWORD,
    CHANGE_USER_DISPLAY_NAME,
    CHANGE_USER_EMAIL,
    CHANGE_USER_ROLES,
    CHANGE_USER_CONFIRM,
    CHANGE_USER_UNCONFIRM,
    CHANGE_USER_LOCK,
    CHANGE_USER_UNLOCK,

    SAVE_AUDIT_EVENT,
    SAVE_STATISTIC_LOG
}
