
const SEARCH_ID_PREFIX = 'MYAPP-';

var search_id;

/**
 * create json for click logs
 */
function createJsonForClickLogs(url, linkNo, searchWords) {
    var data = {
            "search_id": search_id,
            "search_words": searchWords,
            "page_url": url,
            "page_rank": linkNo
        };
    return JSON.stringify(data);
}

/**
 * create json for search products
 */
function createJsonForSearchProducts(searchWords) {
    var data = {
            "query" : [searchWords],
            "manufacturer_ids": [3],
            "weights": [3,2.5,2,1.5,1],
            "start": 1,
            "rows" : 20,
            "debug": true
        };
    return JSON.stringify(data);
}

/**
 * post to website crawler api
 * 
 * @param body json data
 * @param postUrl json data
 */
function doPost(body, path) {
    const request = new Request(
        "http://localhost:8090/api/" + path, 
        {method: "POST", 
         headers: {
            "Content-Type": "application/json",
            "Authorization": "Basic YWRtaW46YWRtaW5wYXNz"
         },
         body: body});

    fetch(request)
        .then(response => {
            if (response.status === 200) {
                if (path == "search_products") {
                    return response.json();
                }
            }
        })
        .then(data => {
            if (path == "search_products") {
                appendSearchProductsResponseToTable(data);
            }
        })
        .catch(error => {
            alert("Error occured \n" + error);
        });
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