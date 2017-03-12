window.onload = function () {
  var ledgerdb = TAFFY();
  var eventdb = TAFFY();
  var eventSizeList = [];
  var eventPositionList = [];
  var host = "http://localhost:9000";

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
            "<tr>" + 
              "<td>" + event["eventId"] + "</td>" + 
              "<td>" + event["eventAttribute"] + "</td>" + 
              "<td>" + event["eventContext"] + "</td>" + 
              "<td>" + event["eventMethod"] + "</td>" + 
              "<td>" + event["eventNanoTime"] + "</td>" + 
              "<td>" + event["eventThread"] + "</td>" + 
              "<td>" + event["eventTimestamp"] + "</td>" + 
              "<td>" + event["eventTriggerMethod"] + "</td>" + 
              "<td>" + event["eventType"] + "</td>" + 
              "<td>" + event["success"] + "</td>" + 
            "</tr>");
        });
    });
  });
}
