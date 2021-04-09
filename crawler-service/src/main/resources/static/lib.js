
const API_ENDPOINT_PREFIX = 'http://localhost:8090/api/';
const SEARCH_ID_PREFIX = 'MYAPP-';

let search_id;


/**
 * create json for click logs
 */
function createJsonForClickLogs(url, rank, searchWords) {
    var data = {
            "search_id": search_id,
            "search_words": searchWords,
            "page_url": url,
            "page_rank": rank
        };
    return JSON.stringify(data);
}

/**
 * create json for search products
 */
function createJsonForSearchProducts(searchWords, siteId, start, rows) {
    var data = {
            "query" : [searchWords],
            "manufacturer_ids": [siteId],
            "start": start,
            "rows" : rows
        };
    return JSON.stringify(data);
}

/**
 * send click logs to click logs API endpoint
 */
function sendClickLog(url, rank, searchWords) {
    return doPost(createJsonForClickLogs(url, rank, searchWords), "click_logs");
}

/**
 * get search product by search words
 */
function searchProduct(searchWords, siteId = 1, start = 0, rows = 20) {
    return doPost(createJsonForSearchProducts(searchWords, siteId, start, rows), "search_products")
    .then(response => {
        return response.json();
    });
}

/**
 * post to website crawler api
 *
 * @param body json data
 * @param postUrl json data
 */
function doPost(body, path) {
    const request = new Request(
        API_ENDPOINT_PREFIX + path,
        {method: "POST",
         headers: {
            "Content-Type": "application/json",
            "Authorization": "Basic YWRtaW46YWRtaW5wYXNz"
         },
         body: body});

    return fetch(request);
}

/**
 * get search ID from UUID
 */
function getSearchId() {
    return createUuid();
}

/**
 * get search ID that append prefix
 */
function refreshSearchId() {
    search_id = SEARCH_ID_PREFIX + createUuid()
}

/**
 * create UUID
 */
function createUuid() {
    var uuid = (function () {
        var S4 = function () {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
        }
        return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
    })();
    return uuid;
}