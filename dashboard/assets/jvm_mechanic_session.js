function getRecordedSessionMap (sessionId, callback) {
  var d = Q.defer()
  storage.getContents('session_data.recorded.session.' + sessionId).then(function (content) {
    if (content) {
      d.resolve(JSON.parse(content))
    }
    if (content && callback) {
      callback(JSON.parse(content))
    } else if (!content && monitorMode === 'playback') {
      //fetch session
      console.log('Fetching session for playback: ' + sessionId)

      $.getJSON(host + '/data/session/?session_id=' + sessionId, function (result) {
        if (isSessionComplete(result)) {
          //session complete, record session
          addRecordedSession(result)
        }

        if (callback) callback(result)
        d.resolve(result)
      }).fail(function (error) {
        d.resolve()
      })
    }
  }).done()
  return d.promise
}

var earliestSessionTimestamp = null
function addRecordedSession (sessionEventList) {
  var earliestEvent = null
  var sessionId = null
  sessionEventList.forEach(function (item, index) {
    var eventTime = item.eventTimestamp
    if (!earliestEvent || moment(earliestEvent).isAfter(moment(eventTime))) {
      earliestEvent = eventTime
    }
    sessionId = item.workSessionId
  })

  if (!earliestSessionTimestamp || moment(earliestEvent).isBefore(moment(earliestSessionTimestamp))) {
    storage.getContents('session_data.timeline.earliest_session').then(function (content) {
      if (content) {
        earliestSessionTimestamp = content
      }
      if (!earliestSessionTimestamp || moment(earliestEvent).isBefore(moment(earliestSessionTimestamp))) {
        earliestSessionTimestamp = earliestEvent
        storage.setContents('session_data.timeline.earliest_session', earliestEvent).then(function () {
          console.log('Set earliest session timestamp to: ' + earliestEvent)
        })
      }
    }).done()
  }

  delete activeSessionMap[sessionId]
  savedRecordedSession(sessionId, sessionEventList)
  saveRecordedSessionToTimelineMap(sessionId, earliestEvent, sessionEventList)
}

var storedSessionMap = {}
function savedRecordedSession (sessionId, sessionEventList) {
  if (storedSessionMap[sessionId] || monitorMode === 'playback') return
  storage.getContents('session_data.recorded.session.' + sessionId).then(function (content) {
    if (!content) {
      storage.setContents('session_data.recorded.session.' + sessionId, JSON.stringify(sessionEventList)).then(function () {
        console.log('Saved recorded session: ' + sessionId)
        storedSessionMap[sessionId] = true
      })
    }
  }).done()
}

var activeSessionMap = {}
function addRecordedEvent (sessionEvent) {
  var sessionEventList = activeSessionMap[sessionEvent.workSessionId]
  if (!sessionEventList) {
    sessionEventList = []
    activeSessionMap[sessionEvent.workSessionId] = sessionEventList
  }

  sessionEventList.push(sessionEvent)
  if (isSessionComplete(sessionEventList)) {
    //session complete, record session
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

    if (eventType === 0 || eventType === 2) {
      //enter/begin
      methodIdChainList.push(methodId)
      if (eventType === 0) {
        hasEnterEvent = true
      }
    } else if (eventType === 1 || eventType === 3) {
      //exit/end
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

var activeTimelineMap = {}
function saveRecordedSessionToTimelineMap (sessionId, earliestEvent) {
  var timelineInterval = moment(earliestEvent).startOf('minute').valueOf()
  var sessionIdList = activeTimelineMap[timelineInterval]
  if (!sessionIdList) {
    sessionIdList = []
    activeTimelineMap[timelineInterval] = sessionIdList
  }

  storage.getContents('session_data.timeline.' + timelineInterval.valueOf()).then(function (content) {
    var storedSessionIdList = []
    if (content) {
      storedSessionIdList = JSON.parse(content)
    }
    if (storedSessionIdList.length > sessionIdList.length) {
      sessionIdList = storedSessionIdList
    }
    sessionIdList.push(sessionId)
    sessionIdList = unique(sessionIdList)

    if (sessionIdList.length > storedSessionIdList.length) {
      storage.setContents('session_data.timeline.' + timelineInterval, JSON.stringify(sessionIdList)).then(function () {
        console.log('Added session to timeline interval: ' + timelineInterval + '; Sessions at timeline interval: ' + sessionIdList.length)
      })
    }
  }).done()
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

  if (monitorMode === 'playback' && startTime && endTime) {
    console.log('Fetching sessions during time for playback! Start time: ' + startTime.format('hh:mm:ss.SSS A') + '; End time: ' + endTime.format('hh:mm:ss.SSS A'))
    $.getJSON(host + '/data/session/time/?start_time=' + startTime.valueOf() + '&end_time=' + endTime.valueOf(), function (result) {
      $.each(result.sessionIdList, function (i, event) {
        getRecordedSessionMap(event, null)
      })
      callback(result.sessionIdList)
    })
    return
  }

  if (!earliestSessionTimestamp) {
    storage.getContents('session_data.timeline.earliest_session').then(function (content) {
      if (content) {
        earliestSessionTimestamp = content
      }

      doWork()
    }).done()
  } else {
    doWork()
  }

  function doWork () {
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

    startTime = moment(startTime)
    endTime = moment(endTime)
    while (startTime.isBefore(endTime)) {
      startTime.startOf('minute')
      endTime.startOf('minute')
      var timelineInterval = startTime
      storage.getContents('session_data.timeline.' + timelineInterval.valueOf()).then(function (content) {
        if (content) {
          callback(JSON.parse(content))
        }
      })

      startTime.add(1, 'minute')
    }
  }
}