
const SEARCH_ID_PREFIX = 'MYAPP-';

var search_id;

/**
 * send click logs to click logs API endpoint
 */
function sendClickLogs(linkNo) {
    doPost(createJsonForClickLogs(linkNo), "click_logs");
}

/**
 * get search product by search words
 */
function searchProduct() {
    removeTableElement();
    refreshSearchId();
    doPost(createJsonForSearchProducts(), "search_products");
}

/**
 * remove table element
 */
function removeTableElement() {
    var div = document.getElementById("responsehere");
    div.textContent = null;
}

/**
 * create json for click logs
 */
function createJsonForClickLogs(linkNo) {
    var data = {
            "search_id": search_id,
            "search_words": document.getElementById("searchwords").value,
            "page_url": document.getElementById("url" + linkNo).textContent,
            "page_rank": linkNo
        };
    return JSON.stringify(data);
}

/**
 * create json for search products
 */
function createJsonForSearchProducts() {
    var searchWords =  document.getElementById("searchwords").value;
    console.log("searchwords=" + searchWords);
    var data = {
            "query" : [searchWords],
            "manufacturer_ids": [2],
            //"weights": [3,2.5,2,1.5,1],
            "start": 1,
            "rows" : 20,
            "debug": false
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
    console.log("body=" + body);
    var postUrl = "http://localhost:8090/api/" + path
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        var READYSTATE_COMPLETED = 4;
        if(this.readyState == READYSTATE_COMPLETED) {
            //alert(this.responseText);
            if (path == "search_products") {
                appendSearchProductsResponseToTable(this.responseText);
            }
        }
        return;
    }
    xhr.open("POST", postUrl);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.setRequestHeader("Authorization", "Basic YWRtaW46YWRtaW5wYXNz");
    xhr.send(body);
}

/**
 * append search products response to table
 * 
 * @param response search producs response
 */
function appendSearchProductsResponseToTable(response) {
    createTable(JSON.parse(response));
}

/**
 * create table element
 * 
 * @param json search producs result
 */
function createTable(json) {
    var root = document.getElementById("responsehere");
    var table = document.createElement("table");
    var thead = document.createElement("thead");
    var td = document.createElement('td');
    td.textContent = "no";
    thead.appendChild(td);
    for (var key in json[0]) {
        td = document.createElement('td');
        td.textContent = key;
        thead.appendChild(td);
    }
    var tbody = document.createElement("tbody");
    for (var i = 0; i < json.length; i++) {
        var tr = document.createElement("tr");
        td = document.createElement("td");
        var a = document.createElement("a");
        a.textContent = i + 1;
        a.href = "javascript:sendClickLogs(" + a.textContent + ");";
        td.appendChild(a);
        tr.appendChild(td);
        for (key in json[i]) {
            td = document.createElement("td");
            td.textContent = json[i][key];
            td.setAttribute("id", key + (i + 1));
            tr.appendChild(td);
            tbody.appendChild(tr);
        }
    }
    table.appendChild(thead);
    table.appendChild(tbody);
    root.appendChild(table);
}

/**
 * get search ID from UUID
 */
function getSearchId() {
    return createUuid();
}

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