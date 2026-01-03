package common.messages;
public enum RequestType {
    // Auth
    LOGIN,
    LOGOUT,
    REGISTER,

    // Catalog
    LIST_CITIES,
    GET_CITIES_COUNT,
    GET_CITY_DETAILS,
    LIST_MAPS_FOR_CITY,
    GET_MAP,
    GET_PENDING_MAPS,

    //guest catalog
    GET_CITY_CATALOG,
    GET_CITY_MAPS,

    // POIs
    LIST_POIS,
    LIST_ROUTES,
    SEARCH_POIS,
    CREATE_POI,      // optional
    UPDATE_POI,      // optional
    GET_POI_INDEX,
    GET_ROUTE_INDEX,      // optional

    // Routes
    LIST_ROUTES_FOR_CITY,
    GET_ROUTE_DETAILS,

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

    // Utility
    PING,

    // Prices
    GET_ALL_CITY_PRICES,
    UPDATE_CITY_PRICE

    }