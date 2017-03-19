//config
var host = "http://localhost:9000";

loadGarbageUpdates();
//update garbage stats every 30 seconds
setInterval(function() {
    loadGarbageUpdates();
}, 30000);

function loadGarbageUpdates() {
    console.log("Downloading latest garbage collection stats...");

    $.getJSON(host + "/gc", function(result) {
        console.log("Updating GC stats...");
        $("#totalGCEvents").text(result.totalGCEvents);
        $("#maxHeapOccupancy").text(result.maxHeapOccupancy + "K");
        $("#maxHeapSpace").text(result.maxHeapSpace + "K");
        $("#maxPermMetaspaceOccupancy").text(result.maxPermMetaspaceOccupancy + "K");
        $("#maxPermMetaspaceSpace").text(result.maxPermMetaspaceSpace + "K");
        $("#GCThroughput").text(result.gcthroughput + "%");
        $("#GCMaxPause").text(moment.duration(result.gcmaxPause).asSeconds() + " seconds");
        $("#GCTotalPause").text(moment.duration(result.gctotalPause).asSeconds() + " seconds");
        $("#stoppedTimeThroughput").text(result.stoppedTimeThroughput + "%");
        $("#stoppedTimeMaxPause").text(moment.duration(result.stoppedTimeMaxPause).asSeconds() + " seconds");
        $("#stoppedTimeTotal").text(moment.duration(result.stoppedTimeTotal).asSeconds() + " seconds");
        $("#GCStoppedRatio").text(result.gcstoppedRatio + "%");
    }).always(function(result) {
        //todo: anything?
    });
}