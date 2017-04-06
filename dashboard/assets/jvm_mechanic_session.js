//session globals
var earliestSessionTimestamp = null
var activeSessionMap = {}
var storedSessionMap = {}
var activeTimelineMap = {}

function getRecordedSessionMap (sessionId, callback) {
  var d = Q.defer()

  if (monitorMode === 'playback') {
    console.log('Fetching session for playback: ' + sessionId)
    $.getJSON(host + '/data/session/?session_id=' + sessionId, function (result) {
      if (callback) {
        callback(result)
      }
      d.resolve(result)
    }).fail(function (error) {
      console.log("ERROR (getRecordedSessionMap): " + error)
      d.resolve()
    })
  } else {
    var result = storedSessionMap[sessionId]
    if (callback) {
      callback(result)
    }
    d.resolve(result)
  }

  return d.promise
}

function addRecordedSession (sessionEventList) {
  var earliestSessionEvent = null
  var sessionId = null
  sessionEventList.forEach(function (item, index) {
    var eventTime = item.eventTimestamp
    if (!earliestSessionEvent || moment(eventTime).isBefore(moment(earliestSessionEvent))) {
      earliestSessionEvent = eventTime
    }
    sessionId = item.workSessionId
  })

  if (!earliestSessionTimestamp || moment(earliestSessionEvent).isBefore(moment(earliestSessionTimestamp))) {
    console.log('Set earliest session timestamp to: ' + earliestSessionEvent)
    earliestSessionTimestamp = earliestSessionEvent
    loadGarbageUpdates(earliestSessionTimestamp)
  }

  delete activeSessionMap[sessionId]
  storedSessionMap[sessionId] = sessionEventList
  saveRecordedSessionToTimelineMap(sessionId, earliestSessionEvent, sessionEventList)
}

function addRecordedEvent (sessionEvent) {
  var sessionEventList = activeSessionMap[sessionEvent.workSessionId]
  if (!sessionEventList) {
    sessionEventList = []
    activeSessionMap[sessionEvent.workSessionId] = sessionEventList
  }

  sessionEventList.push(sessionEvent)
  if (isSessionComplete(sessionEventList)) {
    addRecordedSession(sessionEventList)
  }
}

function isSessionComplete (sessionEventList) {
  var methodIdChainList = []
  var hasEnterEvent = false
  var hasExitEvent = false
  var invalidSession = false

  var singleSessionDB = TAFFY(JSON.stringify(sessionEventList))
  singleSessionDB().order('eventId').each(function (record, recordnumber) {
    var methodId = record['eventMethodId']
    var eventTimestamp = record['eventTimestamp']
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

    if (eventType === 0 || eventType === 2) { //enter/begin
      methodIdChainList.push(methodId)
      if (eventType === 0) {
        hasEnterEvent = true
      }
    } else if (eventType === 1 || eventType === 3) { //exit/end
      var startMethodId = methodIdChainList.pop()
      if (eventType === 1) {
        hasExitEvent = true
      } else if (startMethodId !== methodId) {
        invalidSession = true
      }
    }
  })

  if (invalidSession || methodIdChainList.length > 0 || !hasEnterEvent || !hasExitEvent) {
    return false
  } else {
    return true
  }
}

function saveRecordedSessionToTimelineMap (sessionId, earliestEvent) {
  var timelineInterval = moment(earliestEvent).startOf('minute').valueOf()
  var sessionIdList = activeTimelineMap[timelineInterval]
  if (!sessionIdList) {
    sessionIdList = []
    activeTimelineMap[timelineInterval] = sessionIdList
  }
  sessionIdList.push(sessionId)
  activeTimelineMap[timelineInterval] = unique(sessionIdList)
}

function unique (arr) {
  var u = {}, a = []
  for (var i = 0, l = arr.length; i < l; ++i) {
    if (!u.hasOwnProperty(arr[i])) {
      a.push(arr[i])
      u[arr[i]] = 1
    }
  }
  return a
}

function getSessionTimelineMap (startTime, endTime, callback) {
  if (startTime && !startTime._isAMomentObject) {
    startTime = moment(startTime, 'x')
  }
  if (endTime && !endTime._isAMomentObject) {
    endTime = moment(endTime, 'x')
  }

  if (monitorMode === 'playback') {
    if (!startTime && !endTime) {
      startTime = endTime = -1
      console.log('Fetching sessions during time for playback! Start time: -1; End time: -1')
      $.getJSON(host + '/playback/data/session/time/?start_time=-1&end_time=-1', function (result) {
        $.each(result.allSessionIdList, function (i, event) {
          getRecordedSessionMap(event, null)
        })
        callback(result.allSessionIdList)
      })
    } else {
      console.log('Fetching sessions during time for playback! Start time: ' + startTime.format('hh:mm:ss.SSS A') + '; End time: ' + endTime.format('hh:mm:ss.SSS A'))
      $.getJSON(host + '/data/session/time/?start_time=' + startTime.valueOf() + '&end_time=' + endTime.valueOf(), function (result) {
        $.each(result.sessionIdList, function (i, event) {
          getRecordedSessionMap(event, null)
        })
        callback(result.sessionIdList)
      })
    }
    return
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

  var totalSessionIdList = []
  startTime = moment(startTime).startOf('minute')
  endTime = moment(endTime).startOf('minute')
  while (startTime.isBefore(endTime)) {
    var sessionIdList = activeTimelineMap[startTime.valueOf()]
    if (sessionIdList) {
      totalSessionIdList = totalSessionIdList.concat(sessionIdList)
    }

    startTime.add(1, 'minute')
  }

  if (callback) {
    callback(totalSessionIdList)
  }
}