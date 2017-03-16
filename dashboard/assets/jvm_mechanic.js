window.onload = function () {
    $('#data_work_streams_nav_tab').on('click', function () {
        $('#streamTable').show();
        $('#eventTable').hide();

        var listItems = $("#data_nav_tabs li");
        listItems.each(function(idx, li) {
            if (idx != 0) $(li).remove();
        });
    });
}

function loadAllWorkSessions() {
    var host = "http://localhost:9000";
    var eventdb = TAFFY();
    var configdb = TAFFY();


    sessionDB().order("sessionTimestamp").each(function (record, recordnumber) {
        var workSessionId = record["workSessionId"];
        console.log("Adding session to data: " + workSessionId);

        var successfulSession = true; //todo: check last end work and exit of session
        var max = ledgerdb().filter({workSessionId:workSessionId}).max("eventTimestamp");
        var min = ledgerdb().filter({workSessionId:workSessionId}).min("eventTimestamp");
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

            sessionDB().order("sessionTimestamp").each(function (record, recordnumber) {
                var sessionId = record["workSessionId"];
                var filePosition = 0;
                ledgerdb().filter({workSessionId:sessionId}).order("eventId").each(function (record, recordnumber) {
                    if (record["workSessionId"] == workSessionId) {
                        eventSizeList.push(record["eventSize"]);
                        eventPositionList.push(filePosition);
                    }
                    filePosition += record["eventSize"];
                });
            });

            $('#streamTable').hide();
            $('#eventTable').show();

            var tabList = $('#page-content-wrapper .nav.nav-tabs');
            tabList.append('<li class="nav-item"><a class="nav-link active" href="#">Work Session #' + workSessionId + '</a></li>');

            $.getJSON(host + "/data/event/?event_position=" + eventPositionList.toString() + "&event_size=" + eventSizeList.toString(),
                function(result) {
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
    });
}

function loadStuff() {
    var host = "http://localhost:9000";
    var eventdb = TAFFY();
    var configdb = TAFFY();

    var ledgerSize = ledgerdb().count();
    console.log("cached ledger size: " + ledgerSize);

    $.getJSON(host + "/ledger/?current_ledger_size=" + ledgerSize, function(result){
        $.each(result, function(i, entry){
            ledgerdb.merge(entry, "uniqueEventId");
            sessionDB.merge({workSessionId:entry["workSessionId"], sessionTimestamp:ledgerdb().filter({workSessionId:entry["workSessionId"]}).min("eventTimestamp")}, "workSessionId");
        });

        console.log("Ledger size: " + ledgerdb().count());
        storage.setContents('ledger_data', ledgerdb().stringify()).then(function() {
        });
        loadAllWorkSessions();
    });

    console.log("Ledger size: " + ledgerdb().count());
    storage.setContents('ledger_data', ledgerdb().stringify()).then(function() {
        loadAllWorkSessions();
    });
}


function removePackageName(fullyQuantifiedMethodName) {
    var methodNameArr = fullyQuantifiedMethodName.split(".");
    return methodNameArr[methodNameArr.length - 2]  + "." + methodNameArr[methodNameArr.length - 1];
}
