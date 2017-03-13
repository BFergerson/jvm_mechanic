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

  $.getJSON(host + "/ledger", function(result){
    $.each(result, function(i, entry){
        ledgerdb.insert(entry);
    });

    var filePosition = 0;
    ledgerdb().order("eventId asc").each(function (record, recordnumber) {
      eventSizeList.push(record["eventSize"]);
      eventPositionList.push(filePosition);
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
              "<td>" + event["eventNanoTime"] + "</td>" + 
              "<td>" + event["eventThread"] + "</td>" +
              "<td>" + moment(event["eventTimestamp"]).format("hh:mm:ss a") + "</td>" +
              "<td>" + removePackageName(event["eventTriggerMethod"]) + "</td>" +
              "<td>" + event["eventType"].replace("_EVENT", "") + "</td>" +
              "<td>" + event["success"] + "</td>" +
              "<td>" + (event["eventAttribute"] == null ? "n/a" : event["eventAttribute"]) + "</td>" +
            "</tr>");
        });
    });
  });

  function removePackageName(fullyQuantifiedMethodName) {
    var methodNameArr = fullyQuantifiedMethodName.split(".");
    return methodNameArr[methodNameArr.length - 2]  + "." + methodNameArr[methodNameArr.length - 1];
  }
}
