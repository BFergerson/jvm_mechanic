function ledgerLoaded() {
    loadAllWorkSessions();
}

function addSessionToTable(sessionId, recordedSession) {
    var workSessionId = sessionId;
    console.log("Adding session to data: " + workSessionId);

    var sessionDB = TAFFY(JSON.stringify(recordedSession));
    var successfulSession = true; //todo: check last end work and exit of session
    var max = sessionDB().max("eventTimestamp");
    var min = sessionDB().min("eventTimestamp");
    var sessionDuration = (max - min);
    var streamTableRow = $("<tr" + (successfulSession === false ? " class=\"table-danger\"" : "") + ">" +
        "<td>" + workSessionId + "</td>" +
        "<td>" + moment(min).format("hh:mm:ss.SSS A") + "</td>" +
        "<td>" + moment(max).format("hh:mm:ss.SSS A") + "</td>" +
        "<td>" + sessionDuration + "ms (" + moment.duration(sessionDuration).asSeconds() + " seconds)</td>" +
        "</tr>");
    $("#streamTable > tbody").append(streamTableRow);

    streamTableRow.click(function(event) {
        var eventPositionList = [];
        var eventSizeList = [];
        var filePosition = 0;

        sessionDB().order("sessionTimestamp").each(function (record, recordnumber) {
            var sessionId = record["workSessionId"];
            var workSessionDB = ledgerDB().filter({workSessionId:sessionId});

            workSessionDB.order("ledgerId").each(function (record, recordnumber) {
                if (record["workSessionId"] == workSessionId) {
                    eventSizeList.push(record["eventSize"]);
                    eventPositionList.push(filePosition);
                }
                filePosition += record["eventSize"];
            });
        });

        $('#streamTable').hide();
        $('#eventTable').show();
        $('.row').hide();

        var tabList = $('#page-content-wrapper .nav.nav-tabs');
        tabList.append('<li class="nav-item"><a class="nav-link active" href="#">Work Session #' + workSessionId + '</a></li>');

        $.getJSON(host + "/data/event/?event_position=" + eventPositionList.toString() + "&event_size=" + eventSizeList.toString(), function(result) {
            $("#eventTable > tbody").empty();
            $.each(result, function(i, event){
                eventdb.insert(event); //todo: don't do append per row; do one big update
                $("#eventTable > tbody").append(
                    "<tr" + (event["success"] === false ? " class=\"table-danger\"" : "") + ">" +
                    //"<td>" + event["eventId"] + "</td>" +
                    //"<td>" + event["eventContext"] + "</td>" +
                    "<td>" + removePackageName(event["eventMethod"]) + "</td>" +
                    "<td>" + event["eventThread"] + "</td>" +
                    "<td>" + moment(event["eventTimestamp"]).format("hh:mm:ss.SSS A") + "</td>" +
                    "<td>" + removePackageName(event["eventTriggerMethod"]) + "</td>" +
                    "<td>" + event["eventType"].replace("_EVENT", "") + "</td>" +
                    "</tr>");
            });
        });
    });
}

function loadAllWorkSessions() {
    getSessionTimelineMap(null, null, function(sessionIdList) {
        sessionIdList.forEach(function (item, index) {
            getRecordedSessionMap(item, function(recordedSession) {
                addSessionToTable(item, recordedSession);
            });
        });

        //todo: this better
        setTimeout(function() {
            console.log("Set data table");
            var table;
            if ($.fn.dataTable.isDataTable( '#streamTable' ) ) {
                table = $('#streamTable').DataTable();
            } else {
                table = $('#streamTable').DataTable({
                    "order": [[ 1, "asc" ]]
                });
            }
        }, 5000);
    });
}