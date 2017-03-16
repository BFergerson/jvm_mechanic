window.chartColors = {
    darkred: 'rgb(200,0,0)',
    orange: 'rgb(255, 159, 64)',
    yellow: 'rgb(255, 205, 86)',
    green: 'rgb(75, 192, 192)',
    blue: 'rgb(0,0,255)',
    purple: 'rgb(153, 102, 255)',
    grey: 'rgb(231,233,237)',
    darkslategray: 'rgb(47,79,79)',
    black: 'rgb(0,0,0)',
    peach: 'rgb(255,218,185)',
    coral: 'rgb(255,127,80)',
    skyblue: 'rgb(135,206,235)',
    magenta: 'rgb(255,0,255)'
};

function ledgerLoaded() {
    //relative method duration line chart
    var ctx = document.getElementById("relative_method_runtime_duration_canvas").getContext("2d");
    window.relativeMethodRuntimeDurationLine = new Chart(ctx, relativeMethodRuntimeDurationConfig);

    //absolute method duration line chart
    ctx = document.getElementById("absolute_method_runtime_duration_canvas").getContext("2d");
    window.absoluteMethodRuntimeDurationLine = new Chart(ctx, absoluteMethodRuntimeDurationConfig);


    updateCharts(cutOffMinutesTime);

    //update charts every 5 seconds
    setInterval(function() {
        loadLedgerUpdates();
        updateCharts(cutOffMinutesTime);

        evictOldChartData(relativeMethodRuntimeDurationConfig.data, cutOffMinutesTime);
        evictOldChartData(absoluteMethodRuntimeDurationConfig.data, cutOffMinutesTime);
    }, 5000);
}

var sessionAccountedFor = {};

function isEvictableData(sessionTime, cutOffMinutesTime) {
    var duration = moment.duration(moment().diff(sessionTime));
    var minutes = duration.asMinutes();
    return (minutes >= cutOffMinutesTime);
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

function evictOldChartData(data, cutOffMinutesTime) {
    if (chartHasData(data.datasets)) {
        if (isEvictableData(data.labels[0], cutOffMinutesTime)) {
            console.log("Evicting data more than " + cutOffMinutesTime + " minutes!");
            data.labels.shift();
            data.datasets.forEach(function(dataset) {
                dataset.data.shift();
            });
            window.relativeMethodRuntimeDurationLine.update();
        }
    }
}

function updateCharts(cutOffMinutesTime) {
    console.log("Updating charts...");

    sessionDB().order("sessionTimestamp").each(function (record, recordnumber) {
        var workSessionId = record["workSessionId"];
        var workSessionTime = moment(record["sessionTimestamp"]);

        if (!isEvictableData(workSessionTime, cutOffMinutesTime) && sessionAccountedFor[workSessionId] == null) {
            console.log("Adding session to data: " + workSessionId);
            sessionAccountedFor[workSessionId] = true;
            var effectChainList = [];
            var resultList = [];
            var workSessionDB = ledgerDB().filter({workSessionId:workSessionId});

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
                    relativeMethodRuntimeDurationConfig.data.labels.push(moment(eventTimestamp));
                    absoluteMethodRuntimeDurationConfig.data.labels.push(moment(eventTimestamp));
                    addedTimestamp = true;
                }

                var addedData = false;
                relativeMethodRuntimeDurationConfig.data.datasets.forEach(function(dataset) {
                    if (dataset.label === methodNameMap[eventMethodId]) {
                        dataset.data.push(relativeDuration);
                        addedData = true;
                    }
                });
                absoluteMethodRuntimeDurationConfig.data.datasets.forEach(function(dataset) {
                    if (dataset.label === methodNameMap[eventMethodId]) {
                        dataset.data.push(absoluteDuration);
                        addedData = true;
                    }
                });

                if (addedData === false) {
                    var colorNames = Object.keys(window.chartColors);
                    var colorName = colorNames[relativeMethodRuntimeDurationConfig.data.datasets.length % colorNames.length];
                    var newColor = window.chartColors[colorName];
                    if (relativeMethodRuntimeDurationConfig.data.datasets.length >= colorNames.length) {
                        newColor = getRandomColor();
                    }

                    var newDataset = {
                        label: methodNameMap[eventMethodId],
                        backgroundColor: newColor,
                        borderColor: newColor,
                        data: [],
                        fill: false
                    };

                    newDataset.data.push(relativeDuration);
                    relativeMethodRuntimeDurationConfig.data.datasets.push(newDataset);

                    newDataset = {
                        label: methodNameMap[eventMethodId],
                        backgroundColor: newColor,
                        borderColor: newColor,
                        data: [],
                        fill: false
                    };

                    newDataset.data.push(absoluteDuration);
                    absoluteMethodRuntimeDurationConfig.data.datasets.push(newDataset);
                }
                window.relativeMethodRuntimeDurationLine.update();
                window.absoluteMethodRuntimeDurationLine.update();
            });
        }
    });
}

function getRandomColor() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

function CalculatedMethodDuration(methodId, timestamp) {
    this.methodId = methodId;
    this.relativeDuration = 0;
    this.absoluteDuration = 0;
    this.timestamp = timestamp;
}