
const enterKeyCode = 13;
const displayCaption = ["rank", "score", "url", "title", "digest"];


/**
 * send click logs to API
 */
function doSendClickLog(url, rank) {
    var searchWords = document.getElementById("searchwords").value
    sendClickLog(url, rank, searchWords)
        .then(response => {
            alert("Click on ["  + url + "] has been recorded.");
        })
        .catch(error => {
            alert("Error occured: \n" + error);
        });
}

/**
 * search products by words
 */
function doSearchProduct() {
    removeTableElement();
    refreshSearchId();
    var searchWords = document.getElementById("searchwords").value
    searchProduct(searchWords, 2)
        .then(data => {
            appendSearchProductsResponseToTable(data);
        })
        .catch(error => {
            alert("Error occured \n" + error);
        });
}

/**
 * search products when pressing enter in a text field
 *
 * @param pressed key code
 */
 function onEnter(code) {
    if(code == enterKeyCode) {
        doSearchProduct();
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
 * create table for the search result
 *
 * @param json search producs result
 */
function createTable(json) {
    var root = document.getElementById("responsehere");
    var table = document.createElement("table");
    var thead = document.createElement("thead");
    for (var i = 0; i < displayCaption.length; i++) {
        var th = document.createElement('th');
        th.textContent = displayCaption[i];
        thead.appendChild(th);
    }
    var tbody = document.createElement("tbody");
    for (var j = 0; j < json.length; j++) {
        var tr = document.createElement("tr");
        var tdRank = document.createElement("td");
        tdRank.textContent = j + 1;
        tr.appendChild(tdRank);
        for (var k = 0; k < displayCaption.length; k++) {
            var itemText = json[j][displayCaption[k]];
            if (itemText != undefined) {
                td = document.createElement("td");
                if (displayCaption[k] == "url") {
                    var a = document.createElement("a");
                    a.textContent = itemText;
                    a.href = itemText;
                    (function(url, rank) {
                        a.addEventListener('click', function(e){
                            doSendClickLog(url, rank);
                            e.preventDefault();
                        }, false)
                    })(a.textContent, j + 1)
                    td.appendChild(a);
                } else {
                    td.textContent = itemText;
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
