function getRecordedSessionMap(sessionId, callback) {
    storage.getContents('session_data.recorded.session.' + sessionId).then(function(content) {
        if (content) {
            callback(JSON.parse(content));
        }
    }).done();
}

var earliestSessionTimestamp = null;
function addRecordedSession(sessionEventList) {
    var earliestEvent = null;
    var sessionId = null;
    sessionEventList.forEach(function(item, index) {
        var eventTime = item.eventTimestamp;
        if (earliestEvent == null || moment(earliestEvent).isAfter(moment(eventTime))) {
            earliestEvent = eventTime;
        }
        sessionId = item.workSessionId;
    });

    if (!earliestSessionTimestamp || moment(earliestEvent).isBefore(moment(earliestSessionTimestamp))) {
        storage.getContents('session_data.timeline.earliest_session').then(function(content) {
            if (content) {
                earliestSessionTimestamp = content;
            }
            if (!earliestSessionTimestamp || moment(earliestEvent).isBefore(moment(earliestSessionTimestamp))) {
                earliestSessionTimestamp = earliestEvent;
                storage.setContents('session_data.timeline.earliest_session', earliestEvent).then(function() {
                    console.log("Set earliest session timestamp to: " + earliestEvent);
                });
            }
        }).done();
    }

    delete activeSessionMap[sessionId];
    savedRecordedSession(sessionId, sessionEventList);
    saveRecordedSessionToTimelineMap(sessionId, earliestEvent, sessionEventList);
}

function savedRecordedSession(sessionId, sessionEventList) {
    storage.setContents('session_data.recorded.session.' + sessionId, JSON.stringify(sessionEventList)).then(function() {
         console.log("Saved recorded session: " + sessionId);
     });
}

var activeSessionMap = {};
function addRecordedEvent(sessionEvent) {
    var sessionEventList = activeSessionMap[sessionEvent.workSessionId];
    if (!sessionEventList) {
        sessionEventList = [];
        activeSessionMap[sessionEvent.workSessionId] = sessionEventList;
    }

    sessionEventList.push(sessionEvent);
    if (isSessionComplete(sessionEventList)) {
        //session complete, record session
        addRecordedSession(sessionEventList);
    }
}

function isSessionComplete(sessionEventList) {
    var methodIdChainList = [];
    var hasEnterEvent = false;
    var hasExitEvent = false;
    var invalidSession = false;

    var singleSessionDB = TAFFY(JSON.stringify(sessionEventList));
    singleSessionDB().order("eventId").each(function (record, recordnumber) {
        var methodId = record["eventMethodId"];
        var eventTimestamp = record["eventTimestamp"];
        var eventType = record["eventType"];
        if (eventType == 0 || eventType == 2) {
            //enter/begin
            methodIdChainList.push(methodId);
            if (eventType == 0) {
                hasEnterEvent = true;
            }
        } else if (eventType == 1 || eventType == 3) {
            //exit/end
            var startMethodId = methodIdChainList.pop();
            if (eventType == 1) {
                hasExitEvent = true;
            } else if (startMethodId != methodId) {
                invalidSession = true;
            }
        }
    });

    if (invalidSession || methodIdChainList.length > 0 || !hasEnterEvent || !hasExitEvent) {
        return false;
    } else {
        return true;
    }
}

var activeTimelineMap = {};
function saveRecordedSessionToTimelineMap(sessionId, earliestEvent) {
    var timelineInterval = moment(earliestEvent).startOf('minute').valueOf();
    var sessionIdList = activeTimelineMap[timelineInterval];
    if (!sessionIdList) {
        sessionIdList = [];
        activeTimelineMap[timelineInterval] = sessionIdList;
    }

    storage.getContents('session_data.timeline.' + timelineInterval.valueOf()).then(function(content) {
         var storedSessionIdList = [];
         if (content) {
             storedSessionIdList = JSON.parse(content);
         }
         if (storedSessionIdList.length > sessionIdList.length) {
            sessionIdList = storedSessionIdList;
         }
         sessionIdList.push(sessionId);

         storage.setContents('session_data.timeline.' + timelineInterval, JSON.stringify(sessionIdList)).then(function() {
             console.log("Added session to timeline interval: " + timelineInterval + "; Sessions at timeline interval: " + sessionIdList.length);
         });
     }).done();
}

function getSessionTimelineMap(startTime, endTime, callback) {
    if (!earliestSessionTimestamp) {
        storage.getContents('session_data.timeline.earliest_session').then(function(content) {
            if (content) {
                earliestSessionTimestamp = content;
            }

            doWork();
        }).done();
    } else {
        doWork();
    }

    function doWork() {
        if (!startTime || startTime.isBefore(moment(earliestSessionTimestamp))) {
            startTime = moment(earliestSessionTimestamp);
        }
        if (!endTime || endTime.isAfter(moment())) {
            endTime = moment().add('1', 'minute').startOf('minute');
        }
        while (startTime.isBefore(endTime)) {
            startTime.startOf('minute');
            endTime.startOf('minute');
            var timelineInterval = startTime;
            storage.getContents('session_data.timeline.' + timelineInterval.valueOf()).then(function(content) {
                if (content) {
                    callback(JSON.parse(content));
                }
            });

            startTime.add(1, 'minute');
        }
    }
}