/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 90.0990099009901, "KoPercent": 9.900990099009901};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.900990099009901, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "GET /api/learn/paths"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/user/info"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/learn/my"], "isController": false}, {"data": [1.0, 500, 1500, "POST /api/user/login"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/submit/my"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/problem/list"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/problem/detail/1"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/learn/path/detail/1"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/knowledge-graph/stats"], "isController": false}, {"data": [0.0, 500, 1500, "GET /api/community/posts"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/wrong-book/list"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 1010, 100, 9.900990099009901, 7.195049504950505, 0, 62, 6.0, 13.0, 18.0, 27.889999999999986, 200.7154213036566, 339.41563058922895, 60.831382278418126], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET /api/learn/paths", 100, 0, 0.0, 5.600000000000001, 3, 21, 5.0, 8.900000000000006, 9.949999999999989, 20.949999999999974, 20.968756552736423, 36.92057428182009, 7.208010065003145], "isController": false}, {"data": ["GET /api/user/info", 100, 0, 0.0, 5.74, 3, 41, 4.5, 9.0, 10.0, 40.70999999999985, 20.764119601328904, 16.485575425664454, 7.097111191860465], "isController": false}, {"data": ["GET /api/learn/my", 100, 0, 0.0, 7.260000000000002, 4, 19, 7.0, 10.0, 11.0, 18.97999999999999, 20.903010033444815, 11.880421718227426, 7.124170411789298], "isController": false}, {"data": ["POST /api/user/login", 10, 0, 0.0, 10.299999999999999, 2, 62, 4.5, 56.60000000000002, 62.0, 62.0, 2.2675736961451247, 1.3773738662131518, 0.6089675453514739], "isController": false}, {"data": ["GET /api/submit/my", 100, 0, 0.0, 6.890000000000002, 3, 39, 6.0, 9.0, 11.0, 38.72999999999986, 21.123785382340515, 10.190576151246303, 7.529474281791297], "isController": false}, {"data": ["GET /api/problem/list", 100, 0, 0.0, 16.02, 8, 56, 14.0, 24.900000000000006, 27.94999999999999, 55.84999999999992, 20.483408439164275, 70.29169653830398, 4.140689010651372], "isController": false}, {"data": ["GET /api/problem/detail/1", 100, 0, 0.0, 4.479999999999998, 2, 16, 4.0, 8.0, 9.0, 15.949999999999974, 20.70393374741201, 36.67669513457557, 3.9628623188405796], "isController": false}, {"data": ["GET /api/learn/path/detail/1", 100, 0, 0.0, 11.709999999999997, 6, 33, 11.0, 17.0, 19.0, 32.909999999999954, 21.01281781886951, 78.34661956293338, 7.387318764446312], "isController": false}, {"data": ["GET /api/knowledge-graph/stats", 100, 0, 0.0, 6.579999999999998, 3, 57, 5.0, 9.900000000000006, 10.0, 56.539999999999765, 21.326508850501174, 75.62163441032203, 7.539254105352954], "isController": false}, {"data": ["GET /api/community/posts", 100, 100, 100.0, 1.09, 0, 4, 1.0, 2.0, 2.0, 4.0, 20.77274615704196, 9.858940070627337, 4.26003583298712], "isController": false}, {"data": ["GET /api/wrong-book/list", 100, 0, 0.0, 6.27, 3, 18, 6.0, 9.0, 10.0, 17.929999999999964, 21.272069772388853, 9.807172011274197, 7.395368006807063], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["401", 100, 100.0, 9.900990099009901], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 1010, 100, "401", 100, "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["GET /api/community/posts", 100, 100, "401", 100, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
