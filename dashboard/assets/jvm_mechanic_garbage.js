//config
var host = 'http://localhost:9000'

//global
var activeGarbageTimelineMap = {}

function loadGarbageUpdates () {
  console.log('Downloading latest garbage collection stats...')

  $.getJSON(host + '/gc?start_time=-1&end_time=-1', function (result) {
    console.log('Updating GC stats...')
    $('#totalGCEvents').text(result.totalGCEvents)
    $('#maxHeapOccupancy').text(humanFileSize(result.maxHeapOccupancy))
    $('#maxHeapSpace').text(humanFileSize(result.maxHeapSpace))
    $('#maxPermMetaspaceOccupancy').text(humanFileSize(result.maxPermMetaspaceOccupancy))
    $('#maxPermMetaspaceSpace').text(humanFileSize(result.maxPermMetaspaceSpace))
    $('#GCThroughput').text(result.gcthroughput + '%')
    $('#GCMaxPause').text(moment.duration(result.gcmaxPause).asSeconds() + ' seconds')
    $('#GCTotalPause').text(moment.duration(result.gctotalPause).asSeconds() + ' seconds')
    $('#stoppedTimeThroughput').text(result.stoppedTimeThroughput + '%')
    $('#stoppedTimeMaxPause').text(moment.duration(result.stoppedTimeMaxPause).asSeconds() + ' seconds')
    $('#stoppedTimeTotal').text(moment.duration(result.stoppedTimeTotal).asSeconds() + ' seconds')
    $('#GCStoppedRatio').text(result.gcstoppedRatio + '%')

    //store garbage events
    result.garbageCollectionPauseList.forEach(function (event) {
      saveGarbagePauseToTimelineMap(event)
    })

    updateApplicationThroughputLineChart(result.applicationThroughput)
  }).always(function (result) {
    //todo: anything?
  })
}

function loadPlaybackGarbageReport (startTime, endTime) {
  if (startTime === -1 && endTime === -1) {
    return
  }

  console.log('Downloading latest garbage collection stats...')
  $.getJSON(host + '/gc?start_time=' + startTime + '&end_time=' + endTime, function (result) {
    console.log('Updating GC stats...')
    $('#totalGCEvents').text(result.totalGCEvents)
    $('#maxHeapOccupancy').text(humanFileSize(result.maxHeapOccupancy))
    $('#maxHeapSpace').text(humanFileSize(result.maxHeapSpace))
    $('#maxPermMetaspaceOccupancy').text(humanFileSize(result.maxPermMetaspaceOccupancy))
    $('#maxPermMetaspaceSpace').text(humanFileSize(result.maxPermMetaspaceSpace))
    $('#GCThroughput').text(result.gcthroughput + '%')
    $('#GCMaxPause').text(getPrettyTime(moment.duration(result.gcmaxPause).valueOf()))
    $('#GCTotalPause').text(getPrettyTime(moment.duration(result.gctotalPause).valueOf()))
    $('#stoppedTimeThroughput').text(result.stoppedTimeThroughput + '%')
    $('#stoppedTimeMaxPause').text(getPrettyTime(moment.duration(result.stoppedTimeMaxPause).valueOf()))
    $('#stoppedTimeTotal').text(getPrettyTime(moment.duration(result.stoppedTimeTotal).valueOf()))
    $('#GCStoppedRatio').text(result.gcstoppedRatio + '%')

    //gc insights
    $('#totalAllocatedBytes').text(humanFileSize(result.totalAllocatedBytes))
    $('#totalPromotedBytes').text(humanFileSize(result.totalPromotedBytes))

    var duration = moment.duration(moment(lastSessionTimestamp, 'x').diff(earliestSessionTimestamp))
    var seconds = duration.asSeconds()
    $('#averageAllocationRate').text(humanFileSize(Math.ceil(result.totalAllocatedBytes / seconds)) + '/sec')
    $('#averagePromotionRate').text(humanFileSize(Math.ceil(result.totalPromotedBytes / seconds)) + '/sec')

    updateApplicationThroughputLineChart(result.applicationThroughput)
  }).always(function (result) {
    //todo: anything?
  })
}

function saveGarbagePauseToTimelineMap (garbagePauseEvent) {
  var timelineInterval = moment(garbagePauseEvent.pauseTimestamp).startOf('minute').valueOf()
  var garbagePauseList = activeGarbageTimelineMap[timelineInterval]
  if (!garbagePauseList) {
    garbagePauseList = []
    activeGarbageTimelineMap[timelineInterval] = garbagePauseList
  }
  garbagePauseList.push(garbagePauseEvent)
  garbagePauseList = uniquePause(garbagePauseList)
  console.log('Added garbage pauses to timeline interval: ' + timelineInterval + '; Garbage pauses at timeline interval: ' + garbagePauseList.length)
}

function getGarbagePauseTimelineMap (startTime, endTime, callback) {
  if (startTime && !startTime._isAMomentObject) {
    startTime = moment(startTime, 'x')
  }
  if (endTime && !endTime._isAMomentObject) {
    endTime = moment(endTime, 'x')
  }

  if (!startTime || !earliestSessionTimestamp || startTime.isBefore(moment(earliestSessionTimestamp))) {
    if (earliestSessionTimestamp) {
      startTime = moment(earliestSessionTimestamp)
    } else {
      startTime = moment()
    }
  }
  if (!endTime || endTime.isAfter(moment())) {
    endTime = moment().add('1', 'minute').startOf('minute')
  }

  var totalGarbagePauseList = []
  startTime = moment(startTime).startOf('minute')
  endTime = moment(endTime).startOf('minute')
  while (startTime.isBefore(endTime)) {
    var garbagePauseList = activeGarbageTimelineMap[startTime.valueOf()]
    if (garbagePauseList) {
      totalGarbagePauseList = totalGarbagePauseList.concat(garbagePauseList)
    }

    startTime.add(1, 'minute')
  }

  if (callback) {
    callback(totalGarbagePauseList)
  }
}

function uniquePause (arr) {
  var u = {}, a = []
  for (var i = 0, l = arr.length; i < l; ++i) {
    if (!u[arr[i].pauseTimestamp + '.' + arr[i].pauseDuration]) {
      a.push(arr[i])
      u[arr[i].pauseTimestamp + '.' + arr[i].pauseDuration] = 1
    }
  }
  return a
}