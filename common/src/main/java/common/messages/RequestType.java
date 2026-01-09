package common.messages;
public enum RequestType {
    // Auth
    LOGIN,
    LOGOUT,
    REGISTER,

    // Catalog
    LIST_CITIES,
    GET_CITY_DETAILS,
    GET_MESSAGES,
    LIST_MAPS_FOR_CITY,
    GET_MAP,
    GET_PENDING_MAPS,

    //guest catalog
    GET_CITY_CATALOG,
    GET_CITY_MAPS,

    // POIs
    LIST_POIS,
    SEARCH_POIS,
    CREATE_POI,      // optional
    UPDATE_POI,      // optional
    GET_POI_INDEX,


    // Routes
    SUBMIT_ROUTE,
    LIST_ROUTES_FOR_CITY,
    GET_ROUTE_DETAILS,
    APPROVE_ROUTE,
    GET_PENDING_ROUTES,
    GET_ROUTE_SHEET,
    GET_PENDING_ROUTE_SHEET,
    GET_APPROVED_ROUTES_FOR_CITY,

    // Purchases / Subscriptions
    BUY_MAP,
    BUY_SUBSCRIPTION,
    LIST_USER_PURCHASES,
    LIST_USER_SUBSCRIPTIONS,
    LIST_USER_MAPS,


    // Admin / Manager
    LIST_PENDING_MAP_VERSIONS,
    PEND_MAP,
    APPROVE_MAP_VERSION,
    GET_ACTIVITY_REPORT,
    LIST_ALL_USERS,
    // Utility
    PING,
    GET_REPORT,

    // Prices
    GET_ALL_CITY_PRICES,
    UPDATE_CITY_PRICE

    }