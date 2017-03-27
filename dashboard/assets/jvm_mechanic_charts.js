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

function ledgerLoaded () {
  //bar chat
  ctx = document.getElementById('current_relative_method_runtime_duration_canvas').getContext('2d')
  window.currentMethodDurationBarChart = new Chart(ctx, currentMethodDurationBarChartConfig)

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
  ctx = document.getElementById('total_relative_method_duration_polar_canvas').getContext('2d')
  window.totalMethodDurationPolarChart = new Chart(ctx, totalMethodDurationPolarChartConfig)

  //todo: add invocation count bar chart

  if (monitorMode === 'live') {
    //update charts every 5 seconds
    ledgerUpdated()
    setInterval(function () {
      loadLedgerUpdates()
    }, 3000)
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

  window.currentMethodDurationBarChart.update()
  window.averageMethodDurationPolarChart.update()
  window.totalMethodDurationPolarChart.update()
  window.relativeMethodRuntimeDurationLine.update()
  window.absoluteMethodRuntimeDurationLine.update()
}

function evictOldChartData (data, cutOffMinutesTime) {
  if (chartHasData(data.datasets)) {
    if (isEvictableDataRealTime(data.labels[0], cutOffMinutesTime)) {
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
    }
  }
}

var sessionAccountedForCalcMap = {}
var sessionAccountedFor = {}
var sessionAccountedForEventCount = {}
var methodColorMap = {}
var totalMethodDurationMap = {}
var averageDurationMap = {}
var lastSessionTimestamp = null

function updatePlaybackCharts (startTime, endTime, playbackData) {
  console.log('Updating charts (with playback data)...')
  if (startTime && endTime) {
    earliestSessionTimestamp = startTime.valueOf()
    lastSessionTimestamp = endTime.valueOf()
  }
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

  updateAverageMethodDurationPolarChart(playbackData.averageRelativeMethodDurationMap, playbackData.averageAbsoluteMethodDurationMap)
  updateTotalMethodDurationPolarChart(playbackData.totalRelativeMethodDurationMap, playbackData.totalAbsoluteMethodDurationMap)

  //per method charts
  updatePerMethodCharts(playbackData)

  sessionAccountedForEventCount = playbackData.sessionEventCountMap
  sessionAccountedFor = playbackData.sessionEventCountMap

  updateGeneralMonitoringInformation()

  loadPlaybackGarbageReport(startTime, endTime)
}

function updatePerMethodCharts (playbackData) {
  //sort by longest lived
  var sortedList = []
  Object.keys(playbackData.totalRelativeMethodDurationMap).forEach(function (methodId) {
    var totalLive = playbackData.totalRelativeMethodDurationMap[methodId]
    sortedList.push([methodId, totalLive])
  })
  sortedList.sort(function (a, b) {
    return b[1] - a[1]
  })

  var id = 0
  sortedList.forEach(function (listArr) {
    var methodId = listArr[0]
    var totalLive = playbackData.totalRelativeMethodDurationMap[methodId]
    var totalPauseTime = playbackData.methodGarbagePauseDurationMap[methodId]
    if (!totalPauseTime) {
      totalPauseTime = 0
    }

    var totalRuntime = totalLive - totalPauseTime

    var chart = null
    if (garbageCanvasMap[id]) {
      //update old
      chart = garbageCanvasMap[id]
      chart.data.datasets[0].data = [totalRuntime, totalPauseTime]
    } else {
      //add new
      var ctx = document.getElementById('test_canvas_' + id).getContext('2d')
      chart = new Chart(ctx, {
        type: 'doughnut',
        data: {
          datasets: [],
          labels: []
        }
      })
      chart.data.labels.push('Run')
      chart.data.labels.push('Pause')
      garbageCanvasMap[id] = chart

      var newDataset = {
        data: [totalRuntime, totalPauseTime],
        backgroundColor: [
          methodColorMap[methodId],
          '#8B0000'
        ],
        hoverBackgroundColor: [
          methodColorMap[methodId],
          '#8B0000'
        ]
      }
      chart.data.datasets.push(newDataset)
    }

    //update chart colors
    chart.data.datasets[0].backgroundColor[0] = methodColorMap[methodId]
    chart.data.datasets[0].hoverBackgroundColor[0] = methodColorMap[methodId]

    //update labels
    var methodNameList = getClassMethodParamList(methodNameMap[methodId])
    var headerText = '<b>Class: </b>' + methodNameList[0] + '<br/><b>Method: </b>' + methodNameList[1] + '<br/><b>Params: </b>' + methodNameList[2]
    $('#test_heading_' + id).html(headerText)

    $('#total_run_' + id).text(getPrettyTime(totalRuntime) + ' (' + roundNumber((totalRuntime / totalLive) * 100.00, 2) + '%)')
    $('#total_pause_' + id).text(getPrettyTime(totalPauseTime) + ' (' + roundNumber((totalPauseTime / totalLive) * 100.00, 2) + '%)')
    $('#total_live_' + id).text(getPrettyTime(totalLive))
    $('#invocation_count_' + id).text(playbackData.methodInvocationCountMap[methodId])

    $('#relative_duration_range_' + id).text(getPrettyTime(playbackData.minimumRelativeMethodDurationMap[methodId]) + ' - ' + getPrettyTime(playbackData.maximumRelativeMethodDurationMap[methodId]))
    $('#absolute_duration_range_' + id).text(getPrettyTime(playbackData.minimumAbsoluteMethodDurationMap[methodId]) + ' - ' + getPrettyTime(playbackData.maximumAbsoluteMethodDurationMap[methodId]))

    var duration = moment.duration(moment(lastSessionTimestamp, 'x').diff(earliestSessionTimestamp))
    var hours = duration.asHours()
    var minutes = duration.asMinutes()
    var seconds = duration.asSeconds()
    var invocationCount = playbackData.methodInvocationCountMap[methodId]

    //method frequency
    if (hours > 2 && ((invocationCount / hours) > 100)) {
      $('#execution_frequency_' + id).text((invocationCount / hours) + ' per hour')
    } else if (minutes > 30 && ((invocationCount / minutes) > 100)) {
      $('#execution_frequency_' + id).text(Math.ceil(invocationCount / minutes) + ' per minute')
    } else if (seconds > 0) {
      $('#execution_frequency_' + id).text(Math.ceil(invocationCount / seconds) + ' per second')
    }

    $('#garbage_panel_' + id).show()
    id++
    chart.update()
  })
}

function updateLastSessionBarChart (latestWorkSessionId, latestSessionMethodDurationMap) {
  var newDataset = {
    backgroundColor: [],
    hoverBackgroundColor: [],
    data: []
  }
  if (currentMethodDurationBarChartConfig.data.datasets.length !== 0) {
    newDataset = currentMethodDurationBarChartConfig.data.datasets[0]
    newDataset.data = []
    window.currentMethodDurationBarChart.data.labels = []
  } else {
    currentMethodDurationBarChartConfig.data.datasets.push(newDataset)
  }

  newDataset.label = 'Work Session: ' + latestWorkSessionId
  Object.keys(latestSessionMethodDurationMap).forEach(function (key) {
    var methodDuration = latestSessionMethodDurationMap[key]
    //console.log("Method id: " + methodDuration.methodId + "; Latest (relative): " + methodDuration.relativeDuration);
    window.currentMethodDurationBarChart.data.labels.push(removePackageAndClassName(methodNameMap[methodDuration.methodId]))
    newDataset.backgroundColor.push(methodColorMap[methodDuration.methodId])
    newDataset.data.push(methodDuration.relativeDuration)
  })

  window.currentMethodDurationBarChart.update()
}

function updateAverageMethodDurationPolarChart (averageDurationMap, averageAbsoluteDurationMap) {
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

function updateTotalMethodDurationPolarChart (totalMethodDurationMap, totalAbsoluteMethodDurationMap) {
  var newDataset = {
    backgroundColor: [],
    hoverBackgroundColor: [],
    data: []
  }
  if (totalMethodDurationPolarChartConfig.data.datasets.length !== 0) {
    newDataset = totalMethodDurationPolarChartConfig.data.datasets[0]
    newDataset.data = []
    window.totalMethodDurationPolarChart.data.labels = []
  } else {
    totalMethodDurationPolarChartConfig.data.datasets.push(newDataset)
  }

  Object.keys(totalMethodDurationMap).forEach(function (methodId) {
    var totalMethodDuration = totalMethodDurationMap[methodId]
    //console.log("Method id: " + methodId + "; Total (relative): " + totalMethodDuration);
    window.totalMethodDurationPolarChart.data.labels.push(methodNameMap[methodId])
    newDataset.backgroundColor.push(methodColorMap[methodId])
    newDataset.data.push(totalMethodDuration)
  })

  window.totalMethodDurationPolarChart.update()
}

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
        makeGarbageCharts(startTime, endTime, sessionIdList)
      })
  })
}

var garbageCanvasMap = {}
function makeGarbageCharts (startTime, endTime, sessionList) {
  var invocationCountMap = {}
  getGarbagePauseTimelineMap(startTime, endTime, function (garbagePauseList) {
    var methodRunningMap = {}
    sessionList.forEach(function (sessionId) {
      if (!sessionAccountedForCalcMap[sessionId]) return

      Object.keys(sessionAccountedForCalcMap[sessionId]).forEach(function (key) {
        var calcMethodDuration = sessionAccountedForCalcMap[sessionId][key]
        if (!methodRunningMap[calcMethodDuration.methodId]) {
          methodRunningMap[calcMethodDuration.methodId] = 0
        }
        if (!invocationCountMap[calcMethodDuration.methodId]) {
          invocationCountMap[calcMethodDuration.methodId] = 0
        }
        invocationCountMap[calcMethodDuration.methodId] += calcMethodDuration.invocationCount

        garbagePauseList.forEach(function (garbagePause) {
          calcMethodDuration.actualMethodRuntimeList.forEach(function (methodRunningList) {
            var start = methodRunningList[0]
            var end = methodRunningList[1]

            if (garbagePause.pauseTimestamp >= start && garbagePause.pauseTimestamp <= end) {
              var pauseDuration = end - garbagePause.pauseTimestamp
              if (pauseDuration > garbagePause.pauseDuration) {
                pauseDuration = garbagePause.pauseDuration
              }
              methodRunningMap[calcMethodDuration.methodId] += pauseDuration
            }
          })
        })
      })
    })

    //todo: better way of ordering a map
    var sortList = []
    Object.keys(totalMethodDurationMap).forEach(function (methodId) {
      sortList.push([methodId, totalMethodDurationMap[methodId]])
    })
    sortList.sort(function (a, b) {
      return b[1] - a[1]
    })

    var id = 0
    var updatedMethodMap = {}
    sortList.forEach(function (methodArr) {
      var methodId = methodArr[0]
      var totalLive = totalMethodDurationMap[methodId]
      var totalPauseTime = methodRunningMap[methodId]
      var totalRuntime = totalLive - totalPauseTime

      var chart = null
      if (garbageCanvasMap[methodId]) {
        //update old
        chart = garbageCanvasMap[methodId]
        chart.data.datasets[0].data = [totalRuntime, totalPauseTime]
      } else {
        //add new
        var ctx = document.getElementById('test_canvas_' + id).getContext('2d')
        chart = new Chart(ctx, {
          type: 'doughnut',
          data: {
            datasets: [],
            labels: []
          }
        })
        chart.data.labels.push('Run')
        chart.data.labels.push('Pause')

        var methodNameList = getClassMethodParamList(methodNameMap[methodId])
        var headerText = '<b>Class: </b>' + methodNameList[0] + '<br/><b>Method: </b>' + methodNameList[1] + '<br/><b>Params: </b>' + methodNameList[2]
        $('#test_heading_' + id).html(headerText)
        garbageCanvasMap[methodId] = chart

        var newDataset = {
          data: [totalRuntime, totalPauseTime],
          backgroundColor: [
            methodColorMap[methodId],
            '#8B0000'
          ],
          hoverBackgroundColor: [
            methodColorMap[methodId],
            '#8B0000'
          ]
        }
        chart.data.datasets.push(newDataset)
      }

      chart.update()
      updatedMethodMap[methodId] = true

      //update labels
      $('#total_run_' + id).text(getPrettyTime(totalRuntime) + ' (' + roundNumber((totalRuntime / totalLive) * 100.00, 2) + '%)')
      $('#total_pause_' + id).text(getPrettyTime(totalPauseTime) + ' (' + roundNumber((totalPauseTime / totalLive) * 100.00, 2) + '%)')
      $('#total_live_' + id).text(getPrettyTime(totalLive))
      $('#invocation_count_' + id).text(invocationCountMap[methodId])
      $('#garbage_panel_' + id).show()
      id++
    })
  })
}

function updateGeneralMonitoringInformation () {
  if (earliestSessionTimestamp) {
    $('#earliestSessionTimestamp').text(moment(earliestSessionTimestamp).format('hh:mm:ss.SSS A'))
  }
  if (lastSessionTimestamp) {
    $('#latestSessionTimestamp').text(moment(lastSessionTimestamp).format('hh:mm:ss.SSS A'))
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
      $('#eventRecordingRate').text((eventsAccountedForCount / hours) + ' per hour')
    } else if (minutes > 30 && ((eventsAccountedForCount / minutes) > 100)) {
      $('#eventRecordingRate').text(Math.ceil(eventsAccountedForCount / minutes) + ' per minute')
    } else if (seconds > 0) {
      $('#eventRecordingRate').text(Math.ceil(eventsAccountedForCount / seconds) + ' per second')
    }

    //sessions
    if (hours > 2 && ((sessionAccountedForCount / hours) > 100)) {
      $('#sessionRecordingRate').text((sessionAccountedForCount / hours) + ' per hour')
    } else if (minutes > 30 && ((sessionAccountedForCount / minutes) > 100)) {
      $('#sessionRecordingRate').text(Math.ceil(sessionAccountedForCount / minutes) + ' per minute')
    } else if (seconds > 0) {
      var perSecond = Math.ceil(sessionAccountedForCount / seconds)
      if (perSecond === 1) {
        $('#sessionRecordingRate').text(Math.ceil(sessionAccountedForCount / minutes) + ' per minute')
      } else {
        $('#sessionRecordingRate').text(Math.ceil(sessionAccountedForCount / seconds) + ' per second')
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
    }
    averageDurationMap[eventMethodId].push(relativeDuration)

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

  //update last session bar chart
  if (latestWorkSessionId) {
    var newDataset = {
      backgroundColor: [],
      hoverBackgroundColor: [],
      data: []
    }
    if (currentMethodDurationBarChartConfig.data.datasets.length !== 0) {
      newDataset = currentMethodDurationBarChartConfig.data.datasets[0]
      newDataset.data = []
      window.currentMethodDurationBarChart.data.labels = []
    } else {
      currentMethodDurationBarChartConfig.data.datasets.push(newDataset)
    }

    newDataset.label = 'Work Session: ' + latestWorkSessionId
    Object.keys(latestSessionMethodDurationMap).forEach(function (key) {
      var methodDuration = latestSessionMethodDurationMap[key]
      //console.log("Method id: " + methodDuration.methodId + "; Latest (relative): " + methodDuration.relativeDuration);
      window.currentMethodDurationBarChart.data.labels.push(removePackageAndClassName(methodNameMap[methodDuration.methodId]))
      newDataset.backgroundColor.push(methodColorMap[methodDuration.methodId])
      newDataset.data.push(methodDuration.relativeDuration)
    })
  }

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

  //total method duration polar chat
  var newDataset = {
    backgroundColor: [],
    hoverBackgroundColor: [],
    data: []
  }
  if (totalMethodDurationPolarChartConfig.data.datasets.length !== 0) {
    newDataset = totalMethodDurationPolarChartConfig.data.datasets[0]
    newDataset.data = []
    window.totalMethodDurationPolarChart.data.labels = []
  } else {
    totalMethodDurationPolarChartConfig.data.datasets.push(newDataset)
  }

  Object.keys(totalMethodDurationMap).forEach(function (methodId) {
    var totalMethodDuration = totalMethodDurationMap[methodId]
    //console.log("Method id: " + methodId + "; Total: " + totalMethodDuration);
    window.totalMethodDurationPolarChart.data.labels.push(methodNameMap[methodId])
    newDataset.backgroundColor.push(methodColorMap[methodId])
    newDataset.data.push(totalMethodDuration)
  })

  window.currentMethodDurationBarChart.update()
  window.averageMethodDurationPolarChart.update()
  window.totalMethodDurationPolarChart.update()
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