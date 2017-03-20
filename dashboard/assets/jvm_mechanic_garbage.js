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
        $("#maxHeapOccupancy").text(humanFileSize(result.maxHeapOccupancy));
        $("#maxHeapSpace").text(humanFileSize(result.maxHeapSpace));
        $("#maxPermMetaspaceOccupancy").text(humanFileSize(result.maxPermMetaspaceOccupancy));
        $("#maxPermMetaspaceSpace").text(humanFileSize(result.maxPermMetaspaceSpace));
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

function humanFileSize(bytes) {
    var si = false;
    bytes *= 1024;
    var thresh = si ? 1000 : 1024;
    if(Math.abs(bytes) < thresh) {
        return bytes + ' B';
    }
    var units = si
        ? ['kB','MB','GB','TB','PB','EB','ZB','YB']
        : ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'];
    var u = -1;
    do {
        bytes /= thresh;
        ++u;
    } while(Math.abs(bytes) >= thresh && u < units.length - 1);
    return bytes.toFixed(2)+' '+units[u];
}