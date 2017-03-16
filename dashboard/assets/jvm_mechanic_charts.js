function CalculatedMethodDuration(methodId, timestamp) {
    this.methodId = methodId;
    this.relativeDuration = 0;
    this.absoluteDuration = 0;
    this.timestamp = timestamp;
}

var config = {
    type: 'line',
    data: {
        datasets: []
    },
    options: {
        responsive: true,
        title:{
            display:true,
            text:'Method Runtime Duration - Relative'
        },
        tooltips: {
            mode: 'index',
            intersect: false,
        },
        hover: {
            mode: 'nearest',
            intersect: true
        },
        scales: {
            xAxes: [{
                type: 'time'
            }],
            yAxes: [{
                ticks: {
                    beginAtZero:true
                },
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Execution Time (ms)'
                }
            }]
        }
    }
};

window.onload = function() {
    var ctx = document.getElementById("canvas").getContext("2d");
    window.myLine = new Chart(ctx, config);
};

var colorNames = Object.keys(window.chartColors);


var host = "http://localhost:9000";
var sessionDB = TAFFY();
var methodNameMap = {};
var sessionAccountedFor = {};

$(document).ready(function() {
    setInterval(function() {
        checkForUpdates();
    }, 5000);
});

function checkForUpdates() {
    console.log("Checking for updates...");
    var ledgerSize = ledgerdb().count();
    $.getJSON(host + "/ledger/?current_ledger_size=" + ledgerSize, function(result){
        console.log("Getting ledger...");
        $.each(result, function(i, entry){
            ledgerdb.merge(entry, "uniqueEventId");
            sessionDB.merge({workSessionId:entry["workSessionId"], sessionTimestamp:ledgerdb().filter({workSessionId:entry["workSessionId"]}).min("eventTimestamp")}, "workSessionId");
        });

        console.log("Ledger size: " + ledgerdb().count());
        if (ledgerdb().count() > ledgerSize) {
            //get method names
            var eventPositionList = [];
            var eventSizeList = [];
            var filePosition = 0;

            sessionDB().order("sessionTimestamp").each(function (record, recordnumber) {
                var workSessionId = record["workSessionId"];
                var workSessionDB = ledgerdb().filter({workSessionId:workSessionId});

                workSessionDB.order("eventId").each(function (record, recordnumber) {
                    if (methodNameMap[record["eventMethodId"]] == null) {
                        eventSizeList.push(record["eventSize"]);
                        eventPositionList.push(filePosition);
                        methodNameMap[record["eventMethodId"]] = true;
                    }
                    filePosition += record["eventSize"];
                });
            });

            if (eventPositionList.length > 0) {
                $.getJSON(host + "/data/event/?event_position=" + eventPositionList.toString() + "&event_size=" + eventSizeList.toString(),
                function(result) {
                    $.each(result, function(i, event){
                        methodNameMap[event["eventMethodId"]] = removePackageAndClassName(event["eventMethod"]);
                    });

                    updateCharts();
                });
            } else {
                updateCharts();
            }
        }
    });

    evictOldData();
}

function evictOldData() {
    //evict anything older than cutoffTime
    if (chartHasData(config.data.datasets)) {
        var test = config.data.labels[0];
        var now = moment();
        var duration = moment.duration(moment().diff(test));
        var minutes = duration.minutes();

        if (minutes >= 2) {
            console.log("more than 2 minutes");
            config.data.labels.shift();
            config.data.datasets.forEach(function(dataset) {
                dataset.data.shift();
            });
            window.myLine.update();
        }
    }
}

function chartHasData(datasets) {
    var hasData = false;
    datasets.forEach(function(dataset) {
        if (dataset.data && dataset.data.length > 0) {
            hasData = true;
        }
    });
    return hasData;
}

function updateCharts() {
    console.log("Updating charts...");
    sessionDB().order("sessionTimestamp").each(function (record, recordnumber) {
        var workSessionId = record["workSessionId"];
        var workSessionTime = moment(record["sessionTimestamp"]);
        var now = moment();
        var duration = moment.duration(moment().diff(workSessionTime));
        var minutes = duration.minutes();

        console.log("session: " + workSessionId + "; age: " + minutes + " minutes");
        if (minutes >= 2) {
            //console.log("more than 2 minutes; not adding session: " + workSessionId);
            return;
        }

        if (sessionAccountedFor[workSessionId] == null) {
            console.log("Adding session to data: " + workSessionId);
            sessionAccountedFor[workSessionId] = true;
            var effectChainList = [];
            var resultList = [];
            var workSessionDB = ledgerdb().filter({workSessionId:workSessionId});

            workSessionDB.order("eventId").each(function (record, recordnumber) {
                var methodId = record["eventMethodId"];
                var eventTimestamp = record["eventTimestamp"];
                var eventType = record["eventType"];
                if (eventType == 0 || eventType == 2) {
                    //enter
                    var calcMethodDuration = new CalculatedMethodDuration(methodId, eventTimestamp);
                    effectChainList.push(calcMethodDuration);
                } else if (eventType == 1 || eventType == 3) {
                    //exit
                    var calcMethodDuration = effectChainList.pop();
                    var methodDuration = eventTimestamp - calcMethodDuration.timestamp;
                    calcMethodDuration.relativeDuration += methodDuration;
                    calcMethodDuration.absoluteDuration += methodDuration;
                    resultList.push(calcMethodDuration);

                    if (effectChainList.length > 0) {
                        var parentCalcMethodDuration = effectChainList[effectChainList.length-1];
                        parentCalcMethodDuration.relativeDuration -= methodDuration;
                    }
                }
                //console.log(record["eventMethodId"] + " type: " + eventType);
            });

            var combineList = {};
            resultList.forEach(function(calcMethodDuration) {
                var combineMethodDuration = combineList[calcMethodDuration.methodId];
                if (combineMethodDuration == null) {
                    combineList[calcMethodDuration.methodId] = calcMethodDuration;
                } else {
                    combineMethodDuration.relativeDuration += calcMethodDuration.relativeDuration;
                    combineMethodDuration.absoluteDuration += calcMethodDuration.absoluteDuration;

                    if (calcMethodDuration.timestamp < combineMethodDuration.timestamp) {
                        combineMethodDuration.timestamp = calcMethodDuration.timestamp;
                    }
                }
            });

            var addedTimestamp = false;
            Object.keys(combineList).forEach(function(key) {
                var calcMethodDuration = combineList[key];
                var eventMethodId = calcMethodDuration.methodId;
                var relativeDuration = calcMethodDuration.relativeDuration;
                var absoluteDuration = calcMethodDuration.absoluteDuration;
                var eventTimestamp = calcMethodDuration.timestamp;
                //console.log(moment(eventTimestamp));

                if (addedTimestamp == false) {
                    config.data.labels.push(moment(eventTimestamp));
                    addedTimestamp = true;
                }

                var addedData = false;
                config.data.datasets.forEach(function(dataset) {
                    if (dataset.label === methodNameMap[eventMethodId]) {
                        dataset.data.push(relativeDuration);
                        addedData = true;
                    }
                });

                if (addedData === false) {
                    var colorName = colorNames[config.data.datasets.length % colorNames.length];
                    var newColor = window.chartColors[colorName];
                    var newDataset = {
                        label: methodNameMap[eventMethodId],
                        backgroundColor: newColor,
                        borderColor: newColor,
                        data: [],
                        fill: false
                    };

                    newDataset.data.push(relativeDuration);
                    config.data.datasets.push(newDataset);
                }
                window.myLine.update();
            });
        }
    });
}

function removePackageAndClassName(fullyQuantifiedMethodName) {
    var methodNameArr = fullyQuantifiedMethodName.split(".");
    return methodNameArr[methodNameArr.length - 1];
}