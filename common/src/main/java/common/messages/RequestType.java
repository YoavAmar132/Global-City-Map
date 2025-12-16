package common.messages;
public enum RequestType {
    // Auth
    LOGIN,
    LOGOUT,
    REGISTER,

    // Catalog
    LIST_CITIES,
    GET_CITY_DETAILS,
    LIST_MAPS_FOR_CITY,
    GET_MAP_DETAILS,

    // POIs
    LIST_POIS_FOR_CITY,
    SEARCH_POIS,
    CREATE_POI,      // optional
    UPDATE_POI,      // optional
    DELETE_POI,      // optional

    // Routes
    LIST_ROUTES_FOR_CITY,
    GET_ROUTE_DETAILS,

    // Purchases / Subscriptions
    BUY_MAP,
    BUY_SUBSCRIPTION,
    LIST_USER_PURCHASES,
    LIST_USER_SUBSCRIPTIONS,

    // Admin / Manager
    LIST_PENDING_MAP_VERSIONS,
    PEND_MAP,
    APPROVE_MAP_VERSION,
    GET_ACTIVITY_REPORT,

    // Utility
    PING
}
