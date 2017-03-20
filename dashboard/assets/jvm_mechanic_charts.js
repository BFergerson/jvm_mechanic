var relativeMethodRuntimeDurationConfig = {
    type: 'line',
    data: {
        datasets: [],
        labels: []
    },
    options: {
        responsive: true,
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
                type: 'time',
                time: {
                    displayFormats: {
                        millisecond: 'hh:mm:ss.SSS A',
                        second: 'hh:mm:ss.SSS A',
                        minute: 'hh:mm:ss.SSS A',
                        hour: 'hh:mm:ss.SSS A',
                        day: 'hh:mm:ss.SSS A',
                        week: 'hh:mm:ss.SSS A',
                        month: 'hh:mm:ss.SSS A',
                        quarter: 'hh:mm:ss.SSS A',
                        year: 'hh:mm:ss.SSS A'
                    },
                    tooltipFormat: 'hh:mm:ss.SSS A'
                }
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
var absoluteMethodRuntimeDurationConfig = {
    type: 'line',
    data: {
        datasets: [],
        labels: []
    },
    options: {
        responsive: true,
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
                type: 'time',
                time: {
                    displayFormats: {
                        millisecond: 'hh:mm:ss.SSS A',
                        second: 'hh:mm:ss.SSS A',
                        minute: 'hh:mm:ss.SSS A',
                        hour: 'hh:mm:ss.SSS A',
                        day: 'hh:mm:ss.SSS A',
                        week: 'hh:mm:ss.SSS A',
                        month: 'hh:mm:ss.SSS A',
                        quarter: 'hh:mm:ss.SSS A',
                        year: 'hh:mm:ss.SSS A'
                    },
                    tooltipFormat: 'hh:mm:ss.SSS A'
                }
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

var currentMethodDurationBarChartConfig = {
    type: 'bar',
    datasets: [{
        data: []
    }],
    labels: [],
    options: {
        scales: {
            yAxes: [{
                ticks: {
                    beginAtZero: true
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

var averageMethodDurationPolarChartConfig = {
    type: 'polarArea',
    datasets: [{
        data: []
    }],
    labels: [],
    options: {
        responsive: true,
        elements: {
            animation:{
                animateScale: true
            },
            arc: {
                borderColor: "#000000"
            }
        }
    }
};

var totalMethodDurationPolarChartConfig = {
    type: 'polarArea',
    datasets: [{
        data: []
    }],
    labels: [],
    options: {
        responsive: true,
        elements: {
            animation:{
                animateScale: true
            },
            arc: {
                borderColor: "#000000"
            }
        }
    }
};

window.chartColors = {
    seagreen: 'rgb(34, 95, 111)',
    tan: 'rgb(207, 196, 163)',
    kindablack: 'rgb(50, 46, 48)',
    darkseagreen: 'rgb(140, 181, 181)',
    rosybrown: 'rgb(166, 119, 91)',
    lightsteelblue: 'rgb(215, 215, 217)',
    greyish: 'rgb(135, 132, 132)',
    coral: 'rgb(255,127,80)',
    purple: 'rgb(153, 102, 255)',
    darkslategray: 'rgb(47,79,79)',
    skyblue: 'rgb(135,206,235)',
    sienna: 'rgb(164, 108, 76)',
    magenta: 'rgb(255,0,255)',
    peach: 'rgb(255,218,185)'
};

function ledgerLoaded() {
    //bar chat
    ctx = document.getElementById("current_relative_method_runtime_duration_canvas").getContext("2d");
    window.currentMethodDurationBarChart = new Chart(ctx, currentMethodDurationBarChartConfig);

    //relative method duration line chart
    var ctx = document.getElementById("relative_method_runtime_duration_canvas").getContext("2d");
    window.relativeMethodRuntimeDurationLine = new Chart(ctx, relativeMethodRuntimeDurationConfig);

    //absolute method duration line chart
    ctx = document.getElementById("absolute_method_runtime_duration_canvas").getContext("2d");
    window.absoluteMethodRuntimeDurationLine = new Chart(ctx, absoluteMethodRuntimeDurationConfig);

    //todo
    ctx = document.getElementById("average_relative_method_duration_polar_canvas").getContext("2d");
    window.averageMethodDurationPolarChart = new Chart(ctx, averageMethodDurationPolarChartConfig);

    //todo
    ctx = document.getElementById("total_relative_method_duration_polar_canvas").getContext("2d");
    window.totalMethodDurationPolarChart = new Chart(ctx, totalMethodDurationPolarChartConfig);

    //update charts every 5 seconds
    ledgerUpdated();
    setInterval(function() {
        loadLedgerUpdates();

        //update general monitoring info
        if (earliestSessionTimestamp) {
            $("#earliestSessionTimestamp").text(moment(earliestSessionTimestamp).format("hh:mm:ss.SSS A (M/D/Y)"));
        }
        if (lastSessionTimestamp) {
            $("#latestSessionTimestamp").text(moment(lastSessionTimestamp).format("hh:mm:ss.SSS A (M/D/Y)"));
        }

        //events
        var eventsAccountedForCount = 0;
        Object.keys(sessionAccountedForEventCount).forEach(function(key) {
            var eventCount = sessionAccountedForEventCount[key];
            eventsAccountedForCount += eventCount;
        });
        $("#eventsAccountedFor").text(eventsAccountedForCount);

        //sessions
        var sessionAccountedForCount = 0;
        Object.keys(sessionAccountedFor).forEach(function(key) {
            sessionAccountedForCount++;
        });
        $("#sessionsAccountedFor").text(sessionAccountedForCount);
    }, 5000);
}

function ledgerUpdated() {
    updateCharts(moment().subtract(cutOffMinutesTime, 'minutes'), moment().add(cutOffMinutesTime, 'minutes'));
    evictOldChartData(relativeMethodRuntimeDurationConfig.data, cutOffMinutesTime);
    evictOldChartData(absoluteMethodRuntimeDurationConfig.data, cutOffMinutesTime);
}

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
                if (dataset.sessionId) {
                    delete sessionAccountedFor[dataset.sessionId];
                    delete sessionAccountedForEventCount[dataset.sessionId];
                }
                dataset.data.shift();
            });

            //avg method duration polar chart
            //todo: make own method averages[];
            Object.keys(averageDurationMap).forEach(function(key) {
                var durationList = averageDurationMap[key];
                durationList.pop();
            });
        }
    }
}

var sessionAccountedFor = {};
var sessionAccountedForEventCount = {};
var methodColorMap = {};
var totalMethodDurationMap = {};
var averageDurationMap = {};
var lastSessionTimestamp = null;

function updateCharts(startTime, endTime) {
    console.log("Updating charts...");
    getSessionTimelineMap(startTime, endTime, function(sessionIdList) {
        sessionIdList.forEach(function (item, index) {
            getRecordedSessionMap(item, function(recordedSession) {
                addSessionToCharts(item, recordedSession);
            });
        });
    });
}

function addSessionToCharts(workSessionId, recordedSession) {
    if (sessionAccountedFor[workSessionId]) {
        return;
    }
    console.log("Adding session to chart data: " + workSessionId);
    sessionAccountedFor[workSessionId] = true;
    var effectChainList = [];
    var resultList = [];
    var sessionStartTimestamp = null;
    var sessionEventCount = 0;

    var workSessionDB = TAFFY(JSON.stringify(recordedSession));
    workSessionDB().order("eventId").each(function (record, recordnumber) {
        sessionEventCount++;
        var methodId = record["eventMethodId"];
        var eventTimestamp = record["eventTimestamp"];
        if (!sessionStartTimestamp) {
            sessionStartTimestamp = eventTimestamp;
        }

        var eventType = record["eventType"];
        if (eventType == 0 || eventType == 2) {
            //enter/begin
            var calcMethodDuration = new WorkSessionMethodDuration(methodId, eventTimestamp);
            effectChainList.push(calcMethodDuration);
            calcMethodDuration.invocationCount = 1;
        } else if (eventType == 1 || eventType == 3) {
            //exit/end
            var calcMethodDuration = effectChainList.pop();
            var methodDuration = eventTimestamp - calcMethodDuration.timestamp;
            calcMethodDuration.relativeDuration += methodDuration;
            calcMethodDuration.absoluteDuration += methodDuration;
            calcMethodDuration.invocationCount += 1;
            resultList.push(calcMethodDuration);

            if (effectChainList.length > 0) {
                var parentCalcMethodDuration = effectChainList[effectChainList.length-1];
                parentCalcMethodDuration.relativeDuration -= methodDuration;
            }
        }
    });
    sessionAccountedForEventCount[workSessionId] = sessionEventCount;

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

    if (!lastSessionTimestamp || moment(sessionStartTimestamp).isAfter(moment(lastSessionTimestamp))) {
        lastSessionTimestamp = sessionStartTimestamp;
    }

    latestWorkSessionId = workSessionId;
    latestSessionMethodDurationMap = combineList;
    var addedTimestamp = false;
    Object.keys(combineList).forEach(function(key) {
        var calcMethodDuration = combineList[key];
        var eventMethodId = calcMethodDuration.methodId;
        var relativeDuration = calcMethodDuration.relativeDuration;
        var absoluteDuration = calcMethodDuration.absoluteDuration;
        var eventTimestamp = calcMethodDuration.timestamp;

        if (averageDurationMap[eventMethodId] == null) {
            averageDurationMap[eventMethodId] = [];
        }
        averageDurationMap[eventMethodId].push(relativeDuration);

        if (totalMethodDurationMap[eventMethodId] == null) {
            totalMethodDurationMap[eventMethodId] = 0;
        }
        totalMethodDurationMap[eventMethodId] += relativeDuration;

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

            methodColorMap[eventMethodId] = newColor;
            var newDataset = {
                label: methodNameMap[eventMethodId],
                backgroundColor: newColor,
                borderColor: newColor,
                data: [],
                fill: false,
                sessionId: workSessionId
            };

            newDataset.data.push(relativeDuration);
            relativeMethodRuntimeDurationConfig.data.datasets.push(newDataset);

            newDataset = {
                label: methodNameMap[eventMethodId],
                backgroundColor: newColor,
                borderColor: newColor,
                data: [],
                fill: false,
                sessionId: workSessionId
            };

            newDataset.data.push(absoluteDuration);
            absoluteMethodRuntimeDurationConfig.data.datasets.push(newDataset);
        }
    });

    //update last session bar chart
    if (latestWorkSessionId) {
        var newDataset = {
            backgroundColor: [],
            hoverBackgroundColor: [],
            data: []
        };
        if (currentMethodDurationBarChartConfig.data.datasets.length != 0) {
            newDataset = currentMethodDurationBarChartConfig.data.datasets[0];
            newDataset.data = [];
            window.currentMethodDurationBarChart.data.labels = [];
        } else {
            currentMethodDurationBarChartConfig.data.datasets.push(newDataset);
        }

        newDataset.label = "Work Session: " + latestWorkSessionId;
        Object.keys(latestSessionMethodDurationMap).forEach(function(key) {
            var methodDuration = latestSessionMethodDurationMap[key];
            //console.log("Method id: " + methodDuration.methodId + "; Latest (relative): " + methodDuration.relativeDuration);
            window.currentMethodDurationBarChart.data.labels.push(removePackageAndClassName(methodNameMap[methodDuration.methodId]));
            newDataset.backgroundColor.push(methodColorMap[methodDuration.methodId]);
            newDataset.data.push(methodDuration.relativeDuration);
        });
    }

    //avg method duration polar chat
    var newDataset = {
        backgroundColor: [],
        hoverBackgroundColor: [],
        data: []
    };
    if (averageMethodDurationPolarChartConfig.data.datasets.length != 0) {
        newDataset = averageMethodDurationPolarChartConfig.data.datasets[0];
        newDataset.data = [];
        window.averageMethodDurationPolarChart.data.labels = [];
    } else {
        averageMethodDurationPolarChartConfig.data.datasets.push(newDataset);
    }

    Object.keys(averageDurationMap).forEach(function(methodId) {
        var durationList = averageDurationMap[methodId];
        var durationSum = 0;
        durationList.forEach(function(item, index) {
            durationSum += item;
        });
        //console.log("Method id: " + methodId + "; Avg: " + avg);
        window.averageMethodDurationPolarChart.data.labels.push(methodNameMap[methodId]);
        newDataset.backgroundColor.push(methodColorMap[methodId]);
        newDataset.data.push(durationSum / durationList.length);
    });

    //total method duration polar chat
    var newDataset = {
         backgroundColor: [],
         hoverBackgroundColor: [],
         data: []
    };
    if (totalMethodDurationPolarChartConfig.data.datasets.length != 0) {
        newDataset = totalMethodDurationPolarChartConfig.data.datasets[0];
        newDataset.data = [];
        window.totalMethodDurationPolarChart.data.labels = [];
    } else {
        totalMethodDurationPolarChartConfig.data.datasets.push(newDataset);
    }

    Object.keys(totalMethodDurationMap).forEach(function(methodId) {
        var totalMethodDuration = totalMethodDurationMap[methodId];
        //console.log("Method id: " + methodId + "; Total: " + totalMethodDuration);
        window.totalMethodDurationPolarChart.data.labels.push(methodNameMap[methodId]);
        newDataset.backgroundColor.push(methodColorMap[methodId]);
        newDataset.data.push(totalMethodDuration);
    });

    window.currentMethodDurationBarChart.update();
    window.averageMethodDurationPolarChart.update();
    window.totalMethodDurationPolarChart.update();
    window.relativeMethodRuntimeDurationLine.update();
    window.absoluteMethodRuntimeDurationLine.update();
}

function getRandomColor() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

function WorkSessionMethodDuration(methodId, timestamp) {
    this.methodId = methodId;
    this.relativeDuration = 0;
    this.absoluteDuration = 0;
    this.timestamp = timestamp;
    this.invocationCount = 0;
}