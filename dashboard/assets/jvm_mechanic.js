window.onload = function () {
  var host = "http://localhost:9000";
  var ledgerdb = TAFFY();
  var eventdb = TAFFY();
  var configdb = TAFFY();
  var eventPositionList = [];
  var eventSizeList = [];

  $('#sidebar-toggle-button').on('click', function () {
    $('#sidebar').toggleClass('sidebar-toggle');
    $('#page-content-wrapper').toggleClass('page-content-toggle');
    renderAllCharts();
  });

  $('#data_work_streams_nav_tab').on('click', function () {
      $('#streamTable').show();
      $('#eventTable').hide();

      var listItems = $("#data_nav_tabs li");
      listItems.each(function(idx, li) {
          if (idx != 0) $(li).remove();
      });
   });

  $.getJSON(host + "/ledger", function(result){
    $.each(result, function(i, entry){
        ledgerdb.insert(entry);
    });

    var workSessionArr = ledgerdb().distinct("workSessionId");
    workSessionArr.forEach(function(workSessionId) {
        var max = ledgerdb().filter({workSessionId:workSessionId}).max("eventTimestamp");
        var min = ledgerdb().filter({workSessionId:workSessionId}).min("eventTimestamp");
        var sessionDuration = (max - min);
        var streamTableRow = $("<tr>" +
              "<td>" + workSessionId + "</td>" +
              "<td>" + sessionDuration + "ms (" + moment.duration(sessionDuration).asSeconds() + " seconds)</td>" +
            "</tr>");
        $("#streamTable > tbody").append(streamTableRow);

        streamTableRow.click(function(event) {
            var element = event.target;
            console.log(element);

            $('#streamTable').hide();
            $('#eventTable').show();

            var tabList = $('#page-content-wrapper .nav.nav-tabs');
            tabList.append('<li class="nav-item"><a class="nav-link active" href="#">Work Session #' + workSessionId + '</a></li>');

            var filePosition = 0;
            ledgerdb().order("eventId asc").each(function (record, recordnumber) {
                if (record["workSessionId"] == workSessionId) {
                    eventSizeList.push(record["eventSize"]);
                    eventPositionList.push(filePosition);
                }
                filePosition += record["eventSize"];
            });

            $.getJSON(host + "/data/event/?event_position=" + eventPositionList.toString() + "&event_size=" + eventSizeList.toString(),
                  function(result) {
                    $.each(result, function(i, event){
                      eventdb.insert(event);
                      $("#eventTable > tbody").append(
                        "<tr" + (event["success"] === false ? " class=\"table-danger\"" : "") + ">" +
                          "<td>" + event["eventId"] + "</td>" +
                          "<td>" + event["eventContext"] + "</td>" +
                          "<td>" + removePackageName(event["eventMethod"]) + "</td>" +
                          "<td>" + event["workSessionId"] + "</td>" +
                          "<td>" + event["eventThread"] + "</td>" +
                          "<td>" + moment(event["eventTimestamp"]).format("hh:mm:ss.SSS a") + "</td>" +
                          "<td>" + removePackageName(event["eventTriggerMethod"]) + "</td>" +
                          "<td>" + event["eventType"].replace("_EVENT", "") + "</td>" +
                          "<td>" + event["success"] + "</td>" +
                          "<td>" + (event["eventAttribute"] == null ? "n/a" : event["eventAttribute"]) + "</td>" +
                        "</tr>");
                    });
                });
      });
    });
  });

  function removePackageName(fullyQuantifiedMethodName) {
    var methodNameArr = fullyQuantifiedMethodName.split(".");
    return methodNameArr[methodNameArr.length - 2]  + "." + methodNameArr[methodNameArr.length - 1];
  }
}
