
const enterKeyCode = 13;
const displayCaption = ["no", "score", "url", "title", "digest"];

/**
 * send click logs to click logs API endpoint
 */
function sendClickLogs(url, linkNo) {
    var searchWords = document.getElementById("searchwords").value
    doPost(createJsonForClickLogs(url, linkNo, searchWords), "click_logs");
    alert(url + "is clicked");
}

/**
 * get search product by search words
 */
function searchProduct() {
    removeTableElement();
    refreshSearchId();
    var searchWords = document.getElementById("searchwords").value
    doPost(createJsonForSearchProducts(searchWords), "search_products");
}

/**
 * get search product by search words when you press enter in a text field
 * 
 * @param pressed key code 
 */
 function onEnter(code) {
    if(code == enterKeyCode) {
        searchProduct();
    }
}

/**
 * remove table element
 */
function removeTableElement() {
    var div = document.getElementById("responsehere");
    div.textContent = null;
}

/**
 * append search products response to table
 * 
 * @param response search producs response
 */
function appendSearchProductsResponseToTable(json) {
    createTable(json);
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
    for (var i = 0; i < displayCaption.length; i++) {
        var td = document.createElement('td');
        td.textContent = displayCaption[i];
        thead.appendChild(td);
    }
    var tbody = document.createElement("tbody");
    for (var j = 0; j < json.length; j++) {
        var tr = document.createElement("tr");
        var tdNo = document.createElement("td");
        tdNo.textContent = j + 1;
        tr.appendChild(tdNo);
        for (var k = 0; k < displayCaption.length; k++) {
            var captionDate = json[j][displayCaption[k]];
            if (captionDate != undefined) {
                td = document.createElement("td");
                if (displayCaption[k] == "url") {
                    var a = document.createElement("a");
                    a.textContent = captionDate;
                    a.href = "javascript:void(0);";
                    a.setAttribute("onclick", "sendClickLogs('" + a.textContent + "','" + tdNo.textContent + "')");
                    td.appendChild(a);
                } else {
                    td.textContent = captionDate;
                }
                td.setAttribute("id", displayCaption[k] + (j + 1));
                tr.appendChild(td);
                tbody.appendChild(tr);
            }
        }
    }
    table.appendChild(thead);
    table.appendChild(tbody);
    root.appendChild(table);
}

function isDisplayData(key) {
    for (var i = 0; i < displayCaption.length; i++) {
        if (key == displayCaption[i]) {
            return true;
        }
    }
    return false;
}