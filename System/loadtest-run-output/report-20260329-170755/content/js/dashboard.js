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

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [1.0, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "GET /api/learn/paths"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/user/info"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/learn/my"], "isController": false}, {"data": [1.0, 500, 1500, "POST /api/user/login"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/submit/my"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/problem/list"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/problem/detail/1"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/learn/path/detail/1"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/knowledge-graph/stats"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/community/posts"], "isController": false}, {"data": [1.0, 500, 1500, "GET /api/wrong-book/list"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 1010, 0, 0.0, 8.306930693069305, 2, 73, 7.0, 14.0, 19.0, 28.0, 203.83451059535824, 348.9077478557013, 64.94978241675075], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET /api/learn/paths", 100, 0, 0.0, 6.1000000000000005, 3, 17, 6.0, 9.0, 10.0, 16.95999999999998, 21.41327623126338, 37.703258832976445, 7.360813704496788], "isController": false}, {"data": ["GET /api/user/info", 100, 0, 0.0, 6.350000000000001, 3, 38, 6.0, 10.0, 10.949999999999989, 37.74999999999987, 21.119324181626187, 16.76758843717001, 7.218519007391763], "isController": false}, {"data": ["GET /api/learn/my", 100, 0, 0.0, 8.25, 4, 18, 8.0, 11.900000000000006, 13.0, 17.969999999999985, 21.340162185232607, 12.128881241997439, 7.27316074477166], "isController": false}, {"data": ["POST /api/user/login", 10, 0, 0.0, 11.199999999999998, 3, 66, 5.0, 60.30000000000002, 66.0, 66.0, 2.257336343115124, 1.3711554740406322, 0.606218256207675], "isController": false}, {"data": ["GET /api/submit/my", 100, 0, 0.0, 7.210000000000002, 3, 14, 7.0, 11.0, 12.0, 13.989999999999995, 21.64033758926639, 10.439772235446872, 7.713596894611555], "isController": false}, {"data": ["GET /api/problem/list", 100, 0, 0.0, 17.059999999999988, 8, 73, 15.5, 25.0, 28.0, 72.67999999999984, 20.764119601328904, 71.25499636627907, 4.197434333471761], "isController": false}, {"data": ["GET /api/problem/detail/1", 100, 0, 0.0, 4.840000000000001, 2, 16, 4.5, 7.0, 8.0, 15.989999999999995, 21.07037505267594, 37.32584018120522, 4.0330014749262535], "isController": false}, {"data": ["GET /api/learn/path/detail/1", 100, 0, 0.0, 12.959999999999994, 6, 43, 12.0, 18.0, 20.0, 42.95999999999998, 21.459227467811157, 80.01106491416309, 7.54425965665236], "isController": false}, {"data": ["GET /api/knowledge-graph/stats", 100, 0, 0.0, 6.65, 3, 16, 6.0, 9.900000000000006, 10.949999999999989, 15.95999999999998, 21.867483052700635, 77.5398739886289, 7.730496938552372], "isController": false}, {"data": ["GET /api/community/posts", 100, 0, 0.0, 6.090000000000001, 3, 21, 6.0, 8.900000000000006, 10.949999999999989, 20.91999999999996, 21.26754572522331, 14.538361335601872, 7.705331507868992], "isController": false}, {"data": ["GET /api/wrong-book/list", 100, 0, 0.0, 7.269999999999999, 3, 42, 6.5, 10.0, 12.0, 41.70999999999985, 21.682567215958368, 9.996425764310494, 7.538080008673027], "isController": false}]}, function(index, item){
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
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 1010, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
