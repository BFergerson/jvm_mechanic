//
// CHART CONFIG
//
var applicationThroughputChartConfig = {
  type: 'line',
  data: {
    datasets: [],
    labels: []
  },
  options: {
    legend: {
      display: false
    },
    responsive: true,
    tooltips: {
      mode: 'index',
      intersect: false
    },
    hover: {
      mode: 'nearest',
      intersect: false
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
          suggestedMin: 100
        },
        display: true,
        scaleLabel: {
          display: true,
          labelString: 'Throughput (%)'
        }
      }]
    }
  }
}

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
      intersect: false
    },
    hover: {
      mode: 'nearest',
      intersect: false
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
}
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
      intersect: false
    },
    hover: {
      mode: 'nearest',
      intersect: false
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
}

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
}

var averageMethodDurationPolarChartConfig = {
  type: 'polarArea',
  datasets: [{
    data: []
  }],
  labels: [],
  options: {
    responsive: true,
    elements: {
      animation: {
        animateScale: true
      },
      arc: {
        borderColor: '#000000'
      }
    }
  }
}

var averageAbsoluteMethodDurationPolarChartConfig = {
  type: 'polarArea',
  datasets: [{
    data: []
  }],
  labels: [],
  options: {
    responsive: true,
    elements: {
      animation: {
        animateScale: true
      },
      arc: {
        borderColor: '#000000'
      }
    }
  }
}

var totalMethodDurationPolarChartConfig = {
  type: 'polarArea',
  datasets: [{
    data: []
  }],
  labels: [],
  options: {
    responsive: true,
    elements: {
      animation: {
        animateScale: true
      },
      arc: {
        borderColor: '#000000'
      }
    }
  }
}

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
}

//
// GLOBALS
//
var sessionAccountedForCalcMap = {}
var sessionAccountedFor = {}
var sessionAccountedForEventCount = {}
var methodColorMap = {}
var totalMethodDurationMap = {}
var averageDurationMap = {}
var averageAbsoluteDurationMap = {}
var lastSessionTimestamp = null
var lastGarbageEventMoment = null


//
// FUNCTIONS
//

function ledgerLoaded () {
  //bar chat
//  var ctx = document.getElementById('current_relative_method_runtime_duration_canvas').getContext('2d')
//  window.currentMethodDurationBarChart = new Chart(ctx, currentMethodDurationBarChartConfig)

  //relative method duration line chart
  var ctx = document.getElementById('relative_method_runtime_duration_canvas').getContext('2d')
  window.relativeMethodRuntimeDurationLine = new Chart(ctx, relativeMethodRuntimeDurationConfig)

  //absolute method duration line chart
  ctx = document.getElementById('absolute_method_runtime_duration_canvas').getContext('2d')
  window.absoluteMethodRuntimeDurationLine = new Chart(ctx, absoluteMethodRuntimeDurationConfig)

  //todo
  ctx = document.getElementById('average_relative_method_duration_polar_canvas').getContext('2d')
  window.averageMethodDurationPolarChart = new Chart(ctx, averageMethodDurationPolarChartConfig)

  //todo
  ctx = document.getElementById('average_absolute_method_duration_polar_canvas').getContext('2d')
  window.averageAbsoluteMethodDurationPolarChart = new Chart(ctx, averageAbsoluteMethodDurationPolarChartConfig)

  //todo
//  ctx = document.getElementById('total_relative_method_duration_polar_canvas').getContext('2d')
//  window.totalMethodDurationPolarChart = new Chart(ctx, totalMethodDurationPolarChartConfig)

  //todo
  ctx = document.getElementById('application_throughput_canvas').getContext('2d')
  window.applicationThroughputChart = new Chart(ctx, applicationThroughputChartConfig)

  //todo: add invocation count bar chart

  if (monitorMode === 'live') {
    //update charts every 3 seconds
    ledgerUpdated()
    setInterval(function () {
      loadLedgerUpdates()
    }, 3000)

    //update garbage stats every 6 seconds
    loadGarbageUpdates(moment().subtract(cutOffMinutesTime, 'minutes').valueOf())
    setInterval(function () {
        if (applicationThroughputChartConfig.data.labels[0]) {
            loadGarbageUpdates(applicationThroughputChartConfig.data.labels[0].valueOf())
        }
    }, 6000)
  } else {
    if (monitorMode === 'playback') {
      updatePlaybackRange(-1, -1)
    }
    //updateCharts(null, null) //get it started
  }
}

function ledgerUpdated () {
  updateCharts(moment().subtract(cutOffMinutesTime, 'minutes'), moment().add(cutOffMinutesTime, 'minutes'))
  evictOldChartData(relativeMethodRuntimeDurationConfig.data, cutOffMinutesTime)
  evictOldChartData(absoluteMethodRuntimeDurationConfig.data, cutOffMinutesTime)
}

function isEvictableDataRealTime (sessionTime, cutOffMinutesTime) {
  var duration = moment.duration(moment().diff(sessionTime))
  var minutes = duration.asMinutes()
  return (minutes >= cutOffMinutesTime)
}

function isEvictableData (sessionTime, startTime, endTime) {
  return !sessionTime.isBetween(moment(startTime, 'x'), moment(endTime, 'x'))
}

function chartHasData (datasets) {
  var hasData = false
  datasets.forEach(function (dataset) {
    if (dataset.data && dataset.data.length > 0) {
      hasData = true
    }
  })
  return hasData
}

function evictOldChartDataPlayback (data, startTime, endTime) {
  //todo: don't kill everything? your choice
  data.datasets = []
  data.labels = []
  sessionAccountedForCalcMap = {}
  sessionAccountedFor = {}
  sessionAccountedForEventCount = {}
  totalMethodDurationMap = {}
  averageDurationMap = {}
  averageAbsoluteDurationMap = {}

  //window.currentMethodDurationBarChart.update()
  window.averageMethodDurationPolarChart.update()
  window.averageAbsoluteMethodDurationPolarChart.update()
  //window.totalMethodDurationPolarChart.update()
  window.relativeMethodRuntimeDurationLine.update()
  window.absoluteMethodRuntimeDurationLine.update()
}

function evictOldChartData (data, cutOffMinutesTime) {
  if (chartHasData(data.datasets)) {
    while (isEvictableDataRealTime(data.labels[0], cutOffMinutesTime)) {
      console.log('Evicting data more than ' + cutOffMinutesTime + ' minutes!')

      data.labels.shift()
      data.datasets.forEach(function (dataset) {
        if (dataset.sessionId) {
          console.log('Evicting: ' + dataset.sessionId)
          delete sessionAccountedFor[dataset.sessionId]
          delete sessionAccountedForEventCount[dataset.sessionId]
          delete sessionAccountedForCalcMap[dataset.sessionId]
        }
        dataset.data.shift()
      })

      //avg method duration polar chart
      //todo: make own method averages[];
      Object.keys(averageDurationMap).forEach(function (key) {
        var durationList = averageDurationMap[key]
        durationList.pop()
      })
      Object.keys(averageAbsoluteDurationMap).forEach(function (key) {
        var durationList = averageAbsoluteDurationMap[key]
        durationList.pop()
      })
    }
  }
}

function updatePlaybackCharts (startTime, endTime, playbackData) {
  console.log('Updating charts (with playback data)...')
  evictOldChartDataPlayback(relativeMethodRuntimeDurationConfig.data, startTime, endTime)
  evictOldChartDataPlayback(absoluteMethodRuntimeDurationConfig.data, startTime, endTime)

  //determine method colors
  var i = 0
  Object.keys(playbackData.methodInvocationCountMap).forEach(function (methodId) {
    var colorNames = Object.keys(window.chartColors)
    var colorName = colorNames[i % colorNames.length]
    var newColor = window.chartColors[colorName]
    if (i >= colorNames.length) {
      newColor = getRandomColor()
    }

    methodColorMap[methodId] = newColor
    i++
  })

  var map = {}
  Object.keys(playbackData.sessionTimelineMap).forEach(function (sessionTimestamp) {
    var sessionIdArr = playbackData.sessionTimelineMap[sessionTimestamp]
    var innerMap = {}
    map[sessionTimestamp] = innerMap

    sessionIdArr.forEach(function (sessionId) {
      innerMap[sessionId] = playbackData.sessionRelativeMethodDurationMap[sessionId]
    })
  })
  batchUpdateRelativeLineChart(map)

  var map = {}
  Object.keys(playbackData.sessionTimelineMap).forEach(function (sessionTimestamp) {
    var sessionIdArr = playbackData.sessionTimelineMap[sessionTimestamp]
    var innerMap = {}
    map[sessionTimestamp] = innerMap

    sessionIdArr.forEach(function (sessionId) {
      innerMap[sessionId] = playbackData.sessionAbsoluteMethodDurationMap[sessionId]
    })
  })
  batchUpdateAbsoluteLineChart(map)

  var averageRelativeMethodDurationMap = {}
  var averageAbsoluteMethodDurationMap = {}
  var totalRelativeMethodDurationMap = {}
  var totalAbsoluteMethodDurationMap = {}

  Object.keys(playbackData.relativeMethodDurationStatisticsMap).forEach(function (methodId) {
    averageRelativeMethodDurationMap[methodId] = playbackData.relativeMethodDurationStatisticsMap[methodId].mean
    totalRelativeMethodDurationMap[methodId] = playbackData.relativeMethodDurationStatisticsMap[methodId].sum
  });

  Object.keys(playbackData.absoluteMethodDurationStatisticsMap).forEach(function (methodId) {
    averageAbsoluteMethodDurationMap[methodId] = playbackData.absoluteMethodDurationStatisticsMap[methodId].mean
    totalAbsoluteMethodDurationMap[methodId] = playbackData.absoluteMethodDurationStatisticsMap[methodId].sum
  });

  updateAverageMethodDurationPolarChart(averageRelativeMethodDurationMap)
  updateAbsoluteAverageMethodDurationPolarChart(averageAbsoluteMethodDurationMap)
  //updateTotalMethodDurationPolarChart(totalRelativeMethodDurationMap, totalAbsoluteMethodDurationMap)

  //per method charts
  updatePerMethodCharts(playbackData)

  sessionAccountedForEventCount = playbackData.sessionEventCountMap
  sessionAccountedFor = playbackData.sessionEventCountMap

  updateGeneralMonitoringInformation()

  loadPlaybackGarbageReport(startTime, endTime)
}

function updateApplicationThroughputLineChart (startTime, endTime, applicationThroughput) {
    if (monitorMode === 'playback') {
        applicationThroughputChartConfig.data.labels = []
        applicationThroughputChartConfig.data.datasets = []
        lastGarbageEventMoment = null
    } else {
        if (chartHasData(applicationThroughputChartConfig.data.datasets)) {
              while (isEvictableDataRealTime(applicationThroughputChartConfig.data.labels[0], cutOffMinutesTime)) {
                console.log('Evicting throughput data more than ' + cutOffMinutesTime + ' minutes!')
                applicationThroughputChartConfig.data.labels.shift()
                applicationThroughputChartConfig.data.datasets.forEach(function (dataset) {
                  dataset.data.shift()
                })
             }
          }
    }

  applicationThroughput.throughputMarkerList.forEach(function (throughputMarker) {
    var gcMoment = moment(throughputMarker.timestamp, 'x')
    if ((gcMoment.valueOf() > startTime || startTime === -1) && (gcMoment.valueOf() < endTime || endTime === -1)) {
        if (!lastGarbageEventMoment || lastGarbageEventMoment.isBefore(gcMoment)) {
            //label
            applicationThroughputChartConfig.data.labels.push(gcMoment)

            //data
            var newDataset = {
              backgroundColor: 'rgba(34, 95, 111, 0.7)',
              borderColor: 'rgb(34, 95, 111)',
              data: [throughputMarker.throughputPercent],
              fill: true
            }

            if (applicationThroughputChartConfig.data.datasets.length !== 0) {
              applicationThroughputChartConfig.data.datasets[0].data.push(throughputMarker.throughputPercent)
            } else {
              applicationThroughputChartConfig.data.datasets.push(newDataset)
            }
            lastGarbageEventMoment = gcMoment
        }
    }
  })

  window.applicationThroughputChart.update()
}

function updatePerMethodCharts (playbackData) {
  //sort by longest lived
  var sortedList = []
  Object.keys(playbackData.relativeMethodDurationStatisticsMap).forEach(function (methodId) {
    var totalLive = playbackData.relativeMethodDurationStatisticsMap[methodId].sum
    sortedList.push([methodId, totalLive])
  })
  sortedList.sort(function (a, b) {
    return b[1] - a[1]
  })

  var methodStatsHtml = ''
  var id = 0
  sortedList.forEach(function (listArr) {
    var methodId = listArr[0]
    var totalLive = playbackData.relativeMethodDurationStatisticsMap[methodId].sum
    var totalPauseTime = playbackData.methodGarbagePauseDurationMap[methodId]
    if (!totalPauseTime) {
      totalPauseTime = 0
    }

    var totalRuntime = totalLive - totalPauseTime

    var chart = null
//    if (garbageCanvasMap[id]) {
//      //update old
//      chart = garbageCanvasMap[id]
//      chart.data.datasets[0].data = [totalRuntime, totalPauseTime]
//    } else {
////      //add new
////      var ctx = document.getElementById('test_canvas_' + id).getContext('2d')
////      chart = new Chart(ctx, {
////        type: 'doughnut',
////        data: {
////          datasets: [],
////          labels: []
////        }
////      })
////      chart.data.labels.push('Run')
////      chart.data.labels.push('Pause')
////      garbageCanvasMap[id] = chart
////
////      var newDataset = {
////        data: [totalRuntime, totalPauseTime],
////        backgroundColor: [
////          methodColorMap[methodId],
////          '#8B0000'
////        ],
////        hoverBackgroundColor: [
////          methodColorMap[methodId],
////          '#8B0000'
////        ]
////      }
////      chart.data.datasets.push(newDataset)
//    }
//
//    //update chart colors
//    //chart.data.datasets[0].backgroundColor[0] = methodColorMap[methodId]
//    //chart.data.datasets[0].hoverBackgroundColor[0] = methodColorMap[methodId]

//    $('#total_run_' + id).text(getPrettyTime(totalRuntime) + ' (' + roundNumber((totalRuntime / totalLive) * 100.00, 2) + '%)')
//    $('#total_pause_' + id).text(getPrettyTime(totalPauseTime) + ' (' + roundNumber((totalPauseTime / totalLive) * 100.00, 2) + '%)')
//    $('#total_live_' + id).text(getPrettyTime(totalLive))
//    $('#invocation_count_' + id).text()

    var relHtml = ''
    relHtml += 'Minimum: ' + getPrettyTime(playbackData.relativeMethodDurationStatisticsMap[methodId].min) + '<br/>'
    relHtml += 'Maximum: ' + getPrettyTime(playbackData.relativeMethodDurationStatisticsMap[methodId].max) + '<br/>'
    relHtml += 'Average: ' + getPrettyTime(playbackData.relativeMethodDurationStatisticsMap[methodId].mean) + '<br/>-<br/>'
    relHtml += 'Deviation: ' + roundNumber(playbackData.relativeMethodDurationStatisticsMap[methodId].standardDeviation, 2) + '<br/>'
    relHtml += 'Variance: ' + roundNumber(playbackData.relativeMethodDurationStatisticsMap[methodId].variance, 2) + '<br/>'
    relHtml += 'Total: ' + getPrettyTime(playbackData.relativeMethodDurationStatisticsMap[methodId].sum) + '<br/>'

    var absHtml = ''
    absHtml += 'Minimum: ' + getPrettyTime(playbackData.absoluteMethodDurationStatisticsMap[methodId].min) + '<br/>'
    absHtml += 'Maximum: ' + getPrettyTime(playbackData.absoluteMethodDurationStatisticsMap[methodId].max) + '<br/>'
    absHtml += 'Average: ' + getPrettyTime(playbackData.absoluteMethodDurationStatisticsMap[methodId].mean) + '<br/>-<br/>'
    absHtml += 'Deviation: ' + roundNumber(playbackData.absoluteMethodDurationStatisticsMap[methodId].standardDeviation, 2) + '<br/>'
    absHtml += 'Variance: ' + roundNumber(playbackData.absoluteMethodDurationStatisticsMap[methodId].variance, 2) + '<br/>'
    absHtml += 'Total: ' + getPrettyTime(playbackData.absoluteMethodDurationStatisticsMap[methodId].sum) + '<br/>'

    //method frequency
    var duration = moment.duration(moment(lastSessionTimestamp, 'x').diff(earliestSessionTimestamp))
    var hours = duration.asHours()
    var minutes = duration.asMinutes()
    var seconds = duration.asSeconds()
    var invocationCount = playbackData.methodInvocationCountMap[methodId]

    var methodFrequency = ''
    if (hours > 2 && ((invocationCount / hours) > 100)) {
      methodFrequency = (invocationCount / hours) + '/hour'
    } else if (minutes > 30 && ((invocationCount / minutes) > 100)) {
      methodFrequency = Math.ceil(invocationCount / minutes) + '/min'
    } else if (seconds > 0) {
      methodFrequency = Math.ceil(invocationCount / seconds) + '/sec'
    }

    methodStatsHtml += '<div class="col-sm-3" style="width: 20%; padding-left: 5px; padding-right: 5px"><div class="panel panel-default">'

    var methodNameList = getClassMethodParamList(methodNameMap[methodId])
    var titleHtml = '<b>Class: </b>' + methodNameList[0] + '<br/><b>Method: </b>' + methodNameList[1] + '<br/><b>Params: </b>' + methodNameList[2]
    methodStatsHtml += '<div class="panel-heading" style="white-space: nowrap; overflow: hidden;">'
    methodStatsHtml += '<div class="panel panel-default panel-body" style="background-color: ' + methodColorMap[methodId] + '; padding: 0px; padding-bottom: 25px; margin-top: 5px; margin-bottom: 10px"></div>'
    methodStatsHtml += titleHtml + '</div>'

    //method insights
    if (monitorMode === 'playback') {
        var usedInsightCount = 0
        methodStatsHtml += '<div class="panel panel-default panel-body" style="margin-bottom: 0px"><b>Insights:</b><ul>'

        var constantMethod = false
        playbackData.methodInsights.constantMethodIdSet.forEach(function (constantMethodId) {
            if (methodId == constantMethodId) {
                constantMethod = true
            }
        })
        if (constantMethod) {
            methodStatsHtml += '<li style="color: blue">Constant Method</li>'
            usedInsightCount++
        }

        if (playbackData.methodInsights.highestExecutionCountMethodId == methodId) {
            methodStatsHtml += '<li>Highest Execution Count</li>'
            usedInsightCount++
        } else if (playbackData.methodInsights.lowestExecutionCountMethodId == methodId) {
            methodStatsHtml += '<li>Lowest Execution Count</li>'
            usedInsightCount++
        }

        //relative
        if (playbackData.methodInsights.fastestRelativeMethodDurationMethodId == methodId) {
            methodStatsHtml += '<li style="color: green">Fastest Method Execution</li>'
            usedInsightCount++
        } else if (playbackData.methodInsights.slowestRelativeMethodDurationMethodId == methodId) {
            methodStatsHtml += '<li style="color: red">Slowest Method Execution</li>'
            usedInsightCount++
        }
        if (playbackData.methodInsights.fastestAverageRelativeMethodDurationMethodId == methodId) {
            methodStatsHtml += '<li style="color: green">Fastest Average Method</li>'
            usedInsightCount++
        } else if (playbackData.methodInsights.slowestAverageRelativeMethodDurationMethodId == methodId) {
            methodStatsHtml += '<li style="color: red">Slowest Average Method</li>'
            usedInsightCount++
        }
        if (playbackData.methodInsights.mostVolatileRelativeMethodDurationMethodId == methodId) {
            methodStatsHtml += '<li style="color: red">Most Volatile Method</li>'
            usedInsightCount++
        } else if (playbackData.methodInsights.leastVolatileRelativeMethodDurationMethodId == methodId) {
            methodStatsHtml += '<li style="color: green">Least Volatile Method</li>'
            usedInsightCount++
        }
        if (playbackData.methodInsights.mostVariantRelativeMethodDurationMethodId == methodId) {
            methodStatsHtml += '<li style="color: red">Most Variant Method</li>'
            usedInsightCount++
        } else if (playbackData.methodInsights.leastVariantRelativeMethodDurationMethodId == methodId) {
            methodStatsHtml += '<li style="color: green">Least Variant Method</li>'
            usedInsightCount++
        }
        if (playbackData.methodInsights.longestTotalLivedRelativeMethodId == methodId) {
            methodStatsHtml += '<li>Longest Lived Method</li>'
            usedInsightCount++
        } else if (playbackData.methodInsights.shortestTotalLivedRelativeMethodId == methodId) {
            methodStatsHtml += '<li>Shortest Lived Method</li>'
            usedInsightCount++
        }

//        //absolute
//        if (playbackData.methodInsights.fastestAbsoluteMethodDurationMethodId == methodId) {
//            methodStatsHtml += '<li>Fastest Absolute Method</li>'
//            usedInsightCount++
//        } else if (playbackData.methodInsights.slowestAbsoluteMethodDurationMethodId == methodId) {
//            methodStatsHtml += '<li>Slowest Absolute Method</li>'
//            usedInsightCount++
//        }
//        if (playbackData.methodInsights.fastestAverageAbsoluteMethodDurationMethodId == methodId) {
//            methodStatsHtml += '<li>Fastest Average Absolute Method</li>'
//            usedInsightCount++
//        } else if (playbackData.methodInsights.slowestAverageAbsoluteMethodDurationMethodId == methodId) {
//            methodStatsHtml += '<li>Slowest Average Absolute Method</li>'
//            usedInsightCount++
//        }
//        if (playbackData.methodInsights.mostVolatileAbsoluteMethodDurationMethodId == methodId) {
//            methodStatsHtml += '<li>Most Volatile Absolute Method</li>'
//            usedInsightCount++
//        } else if (playbackData.methodInsights.leastVolatileAbsoluteMethodDurationMethodId == methodId) {
//            methodStatsHtml += '<li>Least Volatile Absolute Method</li>'
//            usedInsightCount++
//        }
//        if (playbackData.methodInsights.mostVariantAbsoluteMethodDurationMethodId == methodId) {
//            methodStatsHtml += '<li>Most Variant Absolute Method</li>'
//            usedInsightCount++
//        } else if (playbackData.methodInsights.leastVariantAbsoluteMethodDurationMethodId == methodId) {
//            methodStatsHtml += '<li>Least Variant Absolute Method</li>'
//            usedInsightCount++
//        }

        var usedBreakerCount = usedInsightCount
        while (usedBreakerCount < 6) {
            methodStatsHtml += '<br/>'
            usedBreakerCount++
        }

        methodStatsHtml += '</div>'
    }

    methodStatsHtml += '<div class="panel-body"><div class="panel panel-default panel-body" style="margin-bottom: 0px">'
    methodStatsHtml += '<b>Invocation Count:</b> ' + playbackData.methodInvocationCountMap[methodId] + '<br/>'
    methodStatsHtml += '<b>Execution Frequency:</b> ' + methodFrequency + '<br/>'
    methodStatsHtml += '<li role="separator" class="sidebar ul divider" style="background-color: #ccc; height: 1px; overflow: hidden; margin-top: 10px; margin-bottom: 15px"></li>'
    methodStatsHtml += '<b>Relative Execution Stats</b><br/>'
    methodStatsHtml += relHtml
    methodStatsHtml += '<li role="separator" class="sidebar ul divider" style="background-color: #ccc; height: 1px; overflow: hidden; margin-top: 10px; margin-bottom: 15px"></li>'
    methodStatsHtml += '<b>Absolute Execution Stats</b><br/>'
    methodStatsHtml += absHtml
    methodStatsHtml += '</div></div></div></div>'

    id++
    //chart.update()
  })

  $('#method_stats_row').html(methodStatsHtml)
}

//function updateLastSessionBarChart (latestWorkSessionId, latestSessionMethodDurationMap) {
//  var newDataset = {
//    backgroundColor: [],
//    hoverBackgroundColor: [],
//    data: []
//  }
//  if (currentMethodDurationBarChartConfig.data.datasets.length !== 0) {
//    newDataset = currentMethodDurationBarChartConfig.data.datasets[0]
//    newDataset.data = []
//    window.currentMethodDurationBarChart.data.labels = []
//  } else {
//    currentMethodDurationBarChartConfig.data.datasets.push(newDataset)
//  }
//
//  newDataset.label = 'Work Session: ' + latestWorkSessionId
//  Object.keys(latestSessionMethodDurationMap).forEach(function (key) {
//    var methodDuration = latestSessionMethodDurationMap[key]
//    //console.log("Method id: " + methodDuration.methodId + "; Latest (relative): " + methodDuration.relativeDuration);
//    window.currentMethodDurationBarChart.data.labels.push(removePackageAndClassName(methodNameMap[methodDuration.methodId]))
//    newDataset.backgroundColor.push(methodColorMap[methodDuration.methodId])
//    newDataset.data.push(methodDuration.relativeDuration)
//  })
//
//  window.currentMethodDurationBarChart.update()
//}

function updateAverageMethodDurationPolarChart (averageDurationMap) {
  var newDataset = {
    backgroundColor: [],
    hoverBackgroundColor: [],
    data: []
  }
  if (averageMethodDurationPolarChartConfig.data.datasets.length !== 0) {
    newDataset = averageMethodDurationPolarChartConfig.data.datasets[0]
    newDataset.data = []
    window.averageMethodDurationPolarChart.data.labels = []
  } else {
    averageMethodDurationPolarChartConfig.data.datasets.push(newDataset)
  }

  Object.keys(averageDurationMap).forEach(function (methodId) {
    var averageDuration = averageDurationMap[methodId]
    //console.log("Method id: " + methodId + "; Avg (relative): " + averageDuration);
    window.averageMethodDurationPolarChart.data.labels.push(methodNameMap[methodId])
    newDataset.backgroundColor.push(methodColorMap[methodId])
    newDataset.data.push(averageDuration)
  })

  window.averageMethodDurationPolarChart.update()
}

function updateAbsoluteAverageMethodDurationPolarChart (averageDurationMap) {
  var newDataset = {
    backgroundColor: [],
    hoverBackgroundColor: [],
    data: []
  }
  if (averageAbsoluteMethodDurationPolarChartConfig.data.datasets.length !== 0) {
    newDataset = averageAbsoluteMethodDurationPolarChartConfig.data.datasets[0]
    newDataset.data = []
    window.averageAbsoluteMethodDurationPolarChart.data.labels = []
  } else {
    averageAbsoluteMethodDurationPolarChartConfig.data.datasets.push(newDataset)
  }

  Object.keys(averageDurationMap).forEach(function (methodId) {
    var averageDuration = averageDurationMap[methodId]
    //console.log("Method id: " + methodId + "; Avg (relative): " + averageDuration);
    window.averageAbsoluteMethodDurationPolarChart.data.labels.push(methodNameMap[methodId])
    newDataset.backgroundColor.push(methodColorMap[methodId])
    newDataset.data.push(averageDuration)
  })

  window.averageAbsoluteMethodDurationPolarChart.update()
}

//function updateTotalMethodDurationPolarChart (totalMethodDurationMap, totalAbsoluteMethodDurationMap) {
//  var newDataset = {
//    backgroundColor: [],
//    hoverBackgroundColor: [],
//    data: []
//  }
//  if (totalMethodDurationPolarChartConfig.data.datasets.length !== 0) {
//    newDataset = totalMethodDurationPolarChartConfig.data.datasets[0]
//    newDataset.data = []
//    window.totalMethodDurationPolarChart.data.labels = []
//  } else {
//    totalMethodDurationPolarChartConfig.data.datasets.push(newDataset)
//  }
//
//  Object.keys(totalMethodDurationMap).forEach(function (methodId) {
//    var totalMethodDuration = totalMethodDurationMap[methodId]
//    //console.log("Method id: " + methodId + "; Total (relative): " + totalMethodDuration);
//    window.totalMethodDurationPolarChart.data.labels.push(methodNameMap[methodId])
//    newDataset.backgroundColor.push(methodColorMap[methodId])
//    newDataset.data.push(totalMethodDuration)
//  })
//
//  window.totalMethodDurationPolarChart.update()
//}

function batchUpdateRelativeLineChart (methodRelativeDurationMap) {
  var orderedTimestampList = Object.keys(methodRelativeDurationMap)
  orderedTimestampList.sort(function (a, b) {
    return a - b
  })

  orderedTimestampList.forEach(function (sessionTimestamp) {
    var methodDurationMap = methodRelativeDurationMap[sessionTimestamp]

    //label
    relativeMethodRuntimeDurationConfig.data.labels.push(moment(sessionTimestamp, 'x'))

    //data
    var map = {}
    Object.keys(methodDurationMap).forEach(function (sessionId) {
      Object.keys(methodDurationMap[sessionId]).forEach(function (methodId) {
        if (!map[methodId]) {
          map[methodId] = []
        }
        map[methodId] = map[methodId].concat(methodDurationMap[sessionId][methodId])
      })
    })

    Object.keys(map).forEach(function (methodId) {
      var relativeDurationArr = map[methodId]

      var addedData = false
      relativeMethodRuntimeDurationConfig.data.datasets.forEach(function (dataset) {
        if (dataset.label === methodNameMap[methodId]) {
          dataset.data = dataset.data.concat(relativeDurationArr)
          addedData = true
        }
      })

      if (addedData === false) {
        var newDataset = {
          label: methodNameMap[methodId],
          backgroundColor: methodColorMap[methodId],
          borderColor: methodColorMap[methodId],
          data: relativeDurationArr,
          fill: false
        }

        relativeMethodRuntimeDurationConfig.data.datasets.push(newDataset)
      }
    })
  })

  window.relativeMethodRuntimeDurationLine.update()
}

function batchUpdateAbsoluteLineChart (methodAbsoluteDurationMap) {
  var orderedTimestampList = Object.keys(methodAbsoluteDurationMap)
  orderedTimestampList.sort(function (a, b) {
    return a - b
  })

  orderedTimestampList.forEach(function (sessionTimestamp) {
    var methodDurationMap = methodAbsoluteDurationMap[sessionTimestamp]

    //label
    absoluteMethodRuntimeDurationConfig.data.labels.push(moment(sessionTimestamp, 'x'))

    //data
    var map = {}
    Object.keys(methodDurationMap).forEach(function (sessionId) {
      Object.keys(methodDurationMap[sessionId]).forEach(function (methodId) {
        if (!map[methodId]) {
          map[methodId] = []
        }
        map[methodId] = map[methodId].concat(methodDurationMap[sessionId][methodId])
      })
    })

    Object.keys(map).forEach(function (methodId) {
      var absoluteDurationArr = map[methodId]

      var addedData = false
      absoluteMethodRuntimeDurationConfig.data.datasets.forEach(function (dataset) {
        if (dataset.label === methodNameMap[methodId]) {
          dataset.data = dataset.data.concat(absoluteDurationArr)
          addedData = true
        }
      })

      if (addedData === false) {
        var newDataset = {
          label: methodNameMap[methodId],
          backgroundColor: methodColorMap[methodId],
          borderColor: methodColorMap[methodId],
          data: absoluteDurationArr,
          fill: false
        }

        absoluteMethodRuntimeDurationConfig.data.datasets.push(newDataset)
      }
    })
  })

  window.absoluteMethodRuntimeDurationLine.update()
}

function singleUpdateLineCharts (eventTimestamp, workSessionId, eventMethodId, relativeDuration, absoluteDuration) {
  relativeMethodRuntimeDurationConfig.data.labels.push(moment(eventTimestamp))
  absoluteMethodRuntimeDurationConfig.data.labels.push(moment(eventTimestamp))

  var addedData = false
  relativeMethodRuntimeDurationConfig.data.datasets.forEach(function (dataset) {
    if (dataset.label === methodNameMap[eventMethodId]) {
      dataset.data.push(relativeDuration)
      addedData = true
    }
  })
  absoluteMethodRuntimeDurationConfig.data.datasets.forEach(function (dataset) {
    if (dataset.label === methodNameMap[eventMethodId]) {
      dataset.data.push(absoluteDuration)
      addedData = true
    }
  })

  if (addedData === false) {
    var newDataset = {
      label: methodNameMap[eventMethodId],
      backgroundColor: methodColorMap[eventMethodId],
      borderColor: methodColorMap[eventMethodId],
      data: [],
      fill: false,
      sessionId: workSessionId
    }

    newDataset.data.push(relativeDuration)
    relativeMethodRuntimeDurationConfig.data.datasets.push(newDataset)

    newDataset = {
      label: methodNameMap[eventMethodId],
      backgroundColor: methodColorMap[eventMethodId],
      borderColor: methodColorMap[eventMethodId],
      data: [],
      fill: false,
      sessionId: workSessionId
    }

    newDataset.data.push(absoluteDuration)
    absoluteMethodRuntimeDurationConfig.data.datasets.push(newDataset)
  }
}

function updateCharts (startTime, endTime) {
  if (monitorMode === 'playback') {
    if (startTime && endTime) {
      earliestSessionTimestamp = startTime.valueOf()
      lastSessionTimestamp = endTime.valueOf()
    }
    evictOldChartDataPlayback(relativeMethodRuntimeDurationConfig.data, startTime, endTime)
    evictOldChartDataPlayback(absoluteMethodRuntimeDurationConfig.data, startTime, endTime)
  }

  console.log('Updating charts...')
  getSessionTimelineMap(startTime, endTime, function (sessionIdList) {
    function eventually (value) {
      return Q.fcall(getRecordedSessionMap, value)
    }

    Q.all(sessionIdList.map(eventually))
      .done(function (result) {
        result.sort(function (a, b) {
          return a[0].eventTimestamp - b[0].eventTimestamp
        })
        result.forEach(function (recordedSession) {
          if (recordedSession) {
            var sessionId = recordedSession[0].workSessionId
            addSessionToCharts(sessionId, recordedSession)
          } else {
            console.log('Skipped empty session!')
          }
        })

        updateGeneralMonitoringInformation()
        //makeGarbageCharts(startTime, endTime, sessionIdList)
      })
  })
}

//var garbageCanvasMap = {}
//function makeGarbageCharts (startTime, endTime, sessionList) {
//  var invocationCountMap = {}
//  getGarbagePauseTimelineMap(startTime, endTime, function (garbagePauseList) {
//    var methodRunningMap = {}
//    sessionList.forEach(function (sessionId) {
//      if (!sessionAccountedForCalcMap[sessionId]) return
//
//      Object.keys(sessionAccountedForCalcMap[sessionId]).forEach(function (key) {
//        var calcMethodDuration = sessionAccountedForCalcMap[sessionId][key]
//        if (!methodRunningMap[calcMethodDuration.methodId]) {
//          methodRunningMap[calcMethodDuration.methodId] = 0
//        }
//        if (!invocationCountMap[calcMethodDuration.methodId]) {
//          invocationCountMap[calcMethodDuration.methodId] = 0
//        }
//        invocationCountMap[calcMethodDuration.methodId] += calcMethodDuration.invocationCount
//
//        garbagePauseList.forEach(function (garbagePause) {
//          calcMethodDuration.actualMethodRuntimeList.forEach(function (methodRunningList) {
//            var start = methodRunningList[0]
//            var end = methodRunningList[1]
//
//            if (garbagePause.pauseTimestamp >= start && garbagePause.pauseTimestamp <= end) {
//              var pauseDuration = end - garbagePause.pauseTimestamp
//              if (pauseDuration > garbagePause.pauseDuration) {
//                pauseDuration = garbagePause.pauseDuration
//              }
//              methodRunningMap[calcMethodDuration.methodId] += pauseDuration
//            }
//          })
//        })
//      })
//    })
//
//    //todo: better way of ordering a map
//    var sortList = []
//    Object.keys(totalMethodDurationMap).forEach(function (methodId) {
//      sortList.push([methodId, totalMethodDurationMap[methodId]])
//    })
//    sortList.sort(function (a, b) {
//      return b[1] - a[1]
//    })
//
//    var id = 0
//    var updatedMethodMap = {}
//    sortList.forEach(function (methodArr) {
//      var methodId = methodArr[0]
//      var totalLive = totalMethodDurationMap[methodId]
//      var totalPauseTime = methodRunningMap[methodId]
//      var totalRuntime = totalLive - totalPauseTime
//
//      var chart = null
//      if (garbageCanvasMap[methodId]) {
//        //update old
//        chart = garbageCanvasMap[methodId]
//        chart.data.datasets[0].data = [totalRuntime, totalPauseTime]
//      } else {
//        //add new
//        var ctx = document.getElementById('test_canvas_' + id).getContext('2d')
//        chart = new Chart(ctx, {
//          type: 'doughnut',
//          data: {
//            datasets: [],
//            labels: []
//          }
//        })
//        chart.data.labels.push('Run')
//        chart.data.labels.push('Pause')
//
//        var methodNameList = getClassMethodParamList(methodNameMap[methodId])
//        var headerText = '<b>Class: </b>' + methodNameList[0] + '<br/><b>Method: </b>' + methodNameList[1] + '<br/><b>Params: </b>' + methodNameList[2]
//        $('#test_heading_' + id).html(headerText)
//        garbageCanvasMap[methodId] = chart
//
//        var newDataset = {
//          data: [totalRuntime, totalPauseTime],
//          backgroundColor: [
//            methodColorMap[methodId],
//            '#8B0000'
//          ],
//          hoverBackgroundColor: [
//            methodColorMap[methodId],
//            '#8B0000'
//          ]
//        }
//        chart.data.datasets.push(newDataset)
//      }
//
//      chart.update()
//      updatedMethodMap[methodId] = true
//
//      //update labels
//      $('#total_run_' + id).text(getPrettyTime(totalRuntime) + ' (' + roundNumber((totalRuntime / totalLive) * 100.00, 2) + '%)')
//      $('#total_pause_' + id).text(getPrettyTime(totalPauseTime) + ' (' + roundNumber((totalPauseTime / totalLive) * 100.00, 2) + '%)')
//      $('#total_live_' + id).text(getPrettyTime(totalLive))
//      $('#invocation_count_' + id).text(invocationCountMap[methodId])
//      $('#garbage_panel_' + id).show()
//      id++
//    })
//  })
//}

function updateGeneralMonitoringInformation () {
  if (earliestSessionTimestamp) {
    $('#earliestSessionTimestamp').text(moment(earliestSessionTimestamp, 'x').format('hh:mm:ss.SSS A'))
  }
  if (lastSessionTimestamp) {
    $('#latestSessionTimestamp').text(moment(lastSessionTimestamp, 'x').format('hh:mm:ss.SSS A'))
  }
  if (earliestSessionTimestamp && lastSessionTimestamp) {
    var duration = moment.duration(moment(lastSessionTimestamp, 'x').diff(moment(earliestSessionTimestamp, 'x')))
    $('#uptimeLabel').text(getPrettyTime(duration.valueOf()))
  }

  //events
  var eventsAccountedForCount = 0
  Object.keys(sessionAccountedForEventCount).forEach(function (key) {
    var eventCount = sessionAccountedForEventCount[key]
    eventsAccountedForCount += eventCount
  })
  $('#eventsAccountedFor').text(eventsAccountedForCount)

  //sessions
  var sessionAccountedForCount = 0
  Object.keys(sessionAccountedFor).forEach(function (key) {
    sessionAccountedForCount++
  })
  $('#sessionsAccountedFor').text(sessionAccountedForCount)

  //calc
  if (earliestSessionTimestamp && lastSessionTimestamp && eventsAccountedForCount > 0) {
    var duration = moment.duration(moment(lastSessionTimestamp, 'x').diff(earliestSessionTimestamp))
    var hours = duration.asHours()
    var minutes = duration.asMinutes()
    var seconds = duration.asSeconds()
    var milliseconds = duration.asMilliseconds()

    //events
    if (hours > 2 && ((eventsAccountedForCount / hours) > 100)) {
      $('#eventRecordingRate').text((eventsAccountedForCount / hours) + '/hour')
    } else if (minutes > 30 && ((eventsAccountedForCount / minutes) > 100)) {
      $('#eventRecordingRate').text(Math.ceil(eventsAccountedForCount / minutes) + '/min')
    } else if (seconds > 0) {
      $('#eventRecordingRate').text(Math.ceil(eventsAccountedForCount / seconds) + '/sec')
    }

    //sessions
    if (hours > 2 && ((sessionAccountedForCount / hours) > 100)) {
      $('#sessionRecordingRate').text((sessionAccountedForCount / hours) + '/hour')
    } else if (minutes > 30 && ((sessionAccountedForCount / minutes) > 100)) {
      $('#sessionRecordingRate').text(Math.ceil(sessionAccountedForCount / minutes) + '/min')
    } else if (seconds > 0) {
      var perSecond = Math.ceil(sessionAccountedForCount / seconds)
      if (perSecond === 1) {
        $('#sessionRecordingRate').text(Math.ceil(sessionAccountedForCount / minutes) + '/min')
      } else {
        $('#sessionRecordingRate').text(Math.ceil(sessionAccountedForCount / seconds) + '/sec')
      }
    }
  }
}

//todo: redo this whole method (specially method duration calc part)
function addSessionToCharts (workSessionId, recordedSession) {
  if (sessionAccountedFor[workSessionId]) {
    return
  }
  sessionAccountedFor[workSessionId] = true
  var effectChainList = []
  var resultList = []
  var sessionStartTimestamp = null
  var sessionEventCount = 0

  var methodStartedTimestamp = null
  var methodEndedTimestamp = null
  var workSessionDB = TAFFY(JSON.stringify(recordedSession))
  workSessionDB().order('eventId').each(function (record, recordnumber) {
    sessionEventCount++
    var methodId = record['eventMethodId']
    var eventTimestamp = record['eventTimestamp']
    if (!sessionStartTimestamp) {
      sessionStartTimestamp = eventTimestamp
    }

    var eventType = record['eventType']
    if (eventType === 'ENTER_EVENT') {
      eventType = 0
    } else if (eventType === 'EXIT_EVENT') {
      eventType = 1
    } else if (eventType === 'BEGIN_WORK_EVENT') {
      eventType = 2
    } else if (eventType === 'END_WORK_EVENT') {
      eventType = 3
    }

    if (eventType === 0 || eventType === 2) {
      //enter/begin
      var calcMethodDuration = new WorkSessionMethodDuration(methodId, eventTimestamp)

      //another method started; add to actualMethodRuntimeList
      var prevCalcMethod = calcMethodDuration
      if (effectChainList.length > 0) {
        prevCalcMethod = effectChainList[effectChainList.length - 1]
      }

      if (methodEndedTimestamp && methodEndedTimestamp !== eventTimestamp) {
        //inbetween method end and method start, goes to parent method
        prevCalcMethod.actualMethodRuntimeList.push([methodEndedTimestamp, eventTimestamp])
      }
      methodStartedTimestamp = eventTimestamp
      methodEndedTimestamp = null

      effectChainList.push(calcMethodDuration)
      calcMethodDuration.invocationCount = 0
    } else if (eventType === 1 || eventType === 3) {
      //exit/end
      if (methodEndedTimestamp) {
        //another method started; add to actualMethodRuntimeList
        var prevCalcMethod = calcMethodDuration
        if (effectChainList.length > 0) {
          prevCalcMethod = effectChainList[effectChainList.length - 1]
        }

        if (eventTimestamp !== methodEndedTimestamp) {
          prevCalcMethod.actualMethodRuntimeList.push([eventTimestamp, methodEndedTimestamp])
        }
        methodEndedTimestamp = eventTimestamp
      } else {
        var prevCalcMethod = calcMethodDuration
        if (effectChainList.length > 0) {
          prevCalcMethod = effectChainList[effectChainList.length - 1]
        }
        if (methodStartedTimestamp !== eventTimestamp) {
          prevCalcMethod.actualMethodRuntimeList.push([methodStartedTimestamp, eventTimestamp])
        }
        methodEndedTimestamp = eventTimestamp
      }

      var calcMethodDuration = effectChainList.pop()
      if (calcMethodDuration) {
        var methodDuration = eventTimestamp - calcMethodDuration.timestamp
        calcMethodDuration.relativeDuration += methodDuration
        calcMethodDuration.absoluteDuration += methodDuration
        calcMethodDuration.invocationCount += 1
        resultList.push(calcMethodDuration)
      }

      if (effectChainList.length > 0) {
        var parentCalcMethodDuration = effectChainList[effectChainList.length - 1]
        parentCalcMethodDuration.relativeDuration -= methodDuration
      }
    }
  })
  sessionAccountedForEventCount[workSessionId] = sessionEventCount

  var combineList = {}
  resultList.forEach(function (calcMethodDuration) {
    var combineMethodDuration = combineList[calcMethodDuration.methodId]
    if (!combineMethodDuration) {
      combineList[calcMethodDuration.methodId] = calcMethodDuration
    } else {
      combineMethodDuration.relativeDuration += calcMethodDuration.relativeDuration
      combineMethodDuration.absoluteDuration += calcMethodDuration.absoluteDuration
      combineMethodDuration.invocationCount += calcMethodDuration.invocationCount

      if (calcMethodDuration.timestamp < combineMethodDuration.timestamp) {
        combineMethodDuration.timestamp = calcMethodDuration.timestamp
      }
    }
  })
  sessionAccountedForCalcMap[workSessionId] = combineList

  if (!lastSessionTimestamp || moment(sessionStartTimestamp).isAfter(moment(lastSessionTimestamp))) {
    lastSessionTimestamp = sessionStartTimestamp
  }

  console.log('Adding session to chart data: ' + workSessionId + '; Time: ' + moment(sessionStartTimestamp, 'x').format('hh:mm:ss.SSS A'))
  latestWorkSessionId = workSessionId
  latestSessionMethodDurationMap = combineList
  var addedTimestamp = false
  Object.keys(combineList).forEach(function (key) {
    var calcMethodDuration = combineList[key]
    var eventMethodId = calcMethodDuration.methodId
    var relativeDuration = calcMethodDuration.relativeDuration
    var absoluteDuration = calcMethodDuration.absoluteDuration
    var eventTimestamp = calcMethodDuration.timestamp

    if (!averageDurationMap[eventMethodId]) {
      averageDurationMap[eventMethodId] = []
      averageAbsoluteDurationMap[eventMethodId] = []
    }
    averageDurationMap[eventMethodId].push(relativeDuration)
    averageAbsoluteDurationMap[eventMethodId].push(absoluteDuration)

    if (!totalMethodDurationMap[eventMethodId]) {
      totalMethodDurationMap[eventMethodId] = 0
    }
    totalMethodDurationMap[eventMethodId] += relativeDuration

    if (addedTimestamp === false) {
      relativeMethodRuntimeDurationConfig.data.labels.push(moment(eventTimestamp))
      absoluteMethodRuntimeDurationConfig.data.labels.push(moment(eventTimestamp))
      addedTimestamp = true
    }

    var addedData = false
    relativeMethodRuntimeDurationConfig.data.datasets.forEach(function (dataset) {
      if (dataset.label === methodNameMap[eventMethodId]) {
        dataset.data.push(relativeDuration)
        addedData = true
      }
    })
    absoluteMethodRuntimeDurationConfig.data.datasets.forEach(function (dataset) {
      if (dataset.label === methodNameMap[eventMethodId]) {
        dataset.data.push(absoluteDuration)
        addedData = true
      }
    })

    if (addedData === false) {
      var colorNames = Object.keys(window.chartColors)
      var colorName = colorNames[relativeMethodRuntimeDurationConfig.data.datasets.length % colorNames.length]
      var newColor = window.chartColors[colorName]
      if (relativeMethodRuntimeDurationConfig.data.datasets.length >= colorNames.length) {
        newColor = getRandomColor()
      }

      methodColorMap[eventMethodId] = newColor
      var newDataset = {
        label: methodNameMap[eventMethodId],
        backgroundColor: newColor,
        borderColor: newColor,
        data: [],
        fill: false,
        sessionId: workSessionId
      }

      newDataset.data.push(relativeDuration)
      relativeMethodRuntimeDurationConfig.data.datasets.push(newDataset)

      newDataset = {
        label: methodNameMap[eventMethodId],
        backgroundColor: newColor,
        borderColor: newColor,
        data: [],
        fill: false,
        sessionId: workSessionId
      }

      newDataset.data.push(absoluteDuration)
      absoluteMethodRuntimeDurationConfig.data.datasets.push(newDataset)
    }
  })

//  //update last session bar chart
//  if (latestWorkSessionId) {
//    var newDataset = {
//      backgroundColor: [],
//      hoverBackgroundColor: [],
//      data: []
//    }
//    if (currentMethodDurationBarChartConfig.data.datasets.length !== 0) {
//      newDataset = currentMethodDurationBarChartConfig.data.datasets[0]
//      newDataset.data = []
//      window.currentMethodDurationBarChart.data.labels = []
//    } else {
//      currentMethodDurationBarChartConfig.data.datasets.push(newDataset)
//    }
//
//    newDataset.label = 'Work Session: ' + latestWorkSessionId
//    Object.keys(latestSessionMethodDurationMap).forEach(function (key) {
//      var methodDuration = latestSessionMethodDurationMap[key]
//      //console.log("Method id: " + methodDuration.methodId + "; Latest (relative): " + methodDuration.relativeDuration);
//      window.currentMethodDurationBarChart.data.labels.push(removePackageAndClassName(methodNameMap[methodDuration.methodId]))
//      newDataset.backgroundColor.push(methodColorMap[methodDuration.methodId])
//      newDataset.data.push(methodDuration.relativeDuration)
//    })
//  }

  //avg method duration polar chat
  var newDataset = {
    backgroundColor: [],
    hoverBackgroundColor: [],
    data: []
  }
  if (averageMethodDurationPolarChartConfig.data.datasets.length !== 0) {
    newDataset = averageMethodDurationPolarChartConfig.data.datasets[0]
    newDataset.data = []
    window.averageMethodDurationPolarChart.data.labels = []
  } else {
    averageMethodDurationPolarChartConfig.data.datasets.push(newDataset)
  }

  Object.keys(averageDurationMap).forEach(function (methodId) {
    var durationList = averageDurationMap[methodId]
    var durationSum = 0
    durationList.forEach(function (item, index) {
      durationSum += item
    })
    //console.log("Method id: " + methodId + "; Avg: " + avg);
    window.averageMethodDurationPolarChart.data.labels.push(methodNameMap[methodId])
    newDataset.backgroundColor.push(methodColorMap[methodId])
    newDataset.data.push(durationSum / durationList.length)
  })

    //avg absolute method duration polar chat
    var newDataset = {
      backgroundColor: [],
      hoverBackgroundColor: [],
      data: []
    }
    if (averageAbsoluteMethodDurationPolarChartConfig.data.datasets.length !== 0) {
      newDataset = averageAbsoluteMethodDurationPolarChartConfig.data.datasets[0]
      newDataset.data = []
      window.averageAbsoluteMethodDurationPolarChart.data.labels = []
    } else {
      averageAbsoluteMethodDurationPolarChartConfig.data.datasets.push(newDataset)
    }

    Object.keys(averageAbsoluteDurationMap).forEach(function (methodId) {
      var durationList = averageAbsoluteDurationMap[methodId]
      var durationSum = 0
      durationList.forEach(function (item, index) {
        durationSum += item
      })
      //console.log("Method id: " + methodId + "; Avg: " + avg);
      window.averageAbsoluteMethodDurationPolarChart.data.labels.push(methodNameMap[methodId])
      newDataset.backgroundColor.push(methodColorMap[methodId])
      newDataset.data.push(durationSum / durationList.length)
    })

//  //total method duration polar chat
//  var newDataset = {
//    backgroundColor: [],
//    hoverBackgroundColor: [],
//    data: []
//  }
//  if (totalMethodDurationPolarChartConfig.data.datasets.length !== 0) {
//    newDataset = totalMethodDurationPolarChartConfig.data.datasets[0]
//    newDataset.data = []
//    window.totalMethodDurationPolarChart.data.labels = []
//  } else {
//    totalMethodDurationPolarChartConfig.data.datasets.push(newDataset)
//  }
//
//  Object.keys(totalMethodDurationMap).forEach(function (methodId) {
//    var totalMethodDuration = totalMethodDurationMap[methodId]
//    //console.log("Method id: " + methodId + "; Total: " + totalMethodDuration);
//    window.totalMethodDurationPolarChart.data.labels.push(methodNameMap[methodId])
//    newDataset.backgroundColor.push(methodColorMap[methodId])
//    newDataset.data.push(totalMethodDuration)
//  })

  //window.currentMethodDurationBarChart.update()
  window.averageMethodDurationPolarChart.update()
  window.averageAbsoluteMethodDurationPolarChart.update()
  //window.totalMethodDurationPolarChart.update()
  window.relativeMethodRuntimeDurationLine.update()
  window.absoluteMethodRuntimeDurationLine.update()
}

function WorkSessionMethodDuration (methodId, timestamp) {
  this.methodId = methodId
  this.relativeDuration = 0
  this.absoluteDuration = 0
  this.timestamp = timestamp
  this.invocationCount = 0
  this.actualMethodRuntimeList = []
}