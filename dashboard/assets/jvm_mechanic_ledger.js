//config
var host = "http://localhost:9000";

//init dbs
var ledgerDB = TAFFY();
var sessionDB = TAFFY();
var methodNameMap = {};

//load local storage
var desiredCapacity = 50 * 1024 * 1024; // 50MB
var storage = new LargeLocalStorage({
    size: desiredCapacity
});
storage.initialized.then(function() {
    storage.getContents('ledger_data').then(function(content) {
        console.log("Looking if cache has data...");
        if (content) {
            ledgerDB = TAFFY(content);
            ledgerDB().each(function (record, recordnumber) {
                sessionDB.merge({workSessionId:record["workSessionId"], sessionTimestamp:ledgerDB().filter({workSessionId:record["workSessionId"]}).min("eventTimestamp")}, "workSessionId");
            });
            console.log("Got ledger from cache! Size: " + ledgerDB().count());
        } else {
            console.log("Nothing in cache!");
        }

        loadLedgerUpdates(true);
    });

    console.log("Local storage size: " + storage.size);
    console.log("Local storage capacity: " + storage.getCapacity() + " bytes");
}, function() {
    console.log('denied');
    //todo: fail in some spectacular way
});

function loadLedgerUpdates() {
    loadLedgerUpdates(false);
}

function loadLedgerUpdates(initialLoad) {
    console.log("Checking for ledger updates...");
    var ledgerSize = ledgerDB().count();
    var ledgerUpdated = false;

    $.getJSON(host + "/ledger/?current_ledger_size=" + ledgerSize, function(result) {
        ledgerUpdated = true;
        console.log("Got ledger updates! Size: " + result.length);
        $.each(result, function(i, entry){
            ledgerDB.merge(entry, "uniqueEventId");
            sessionDB.merge({workSessionId:entry["workSessionId"], sessionTimestamp:ledgerDB().filter({workSessionId:entry["workSessionId"]}).min("eventTimestamp")}, "workSessionId");
        });

        console.log("New Ledger size: " + ledgerDB().count());
    }).always(function(result) {
        if (!ledgerUpdated && !initialLoad) {
            console.log("No ledger updates found! Size: " + ledgerSize);
        } else if (initialLoad) {
            loadMethodNames();
        } else {
            //save ledger
            storage.setContents('ledger_data', ledgerDB().stringify()).then(function() {
                console.log("Ledger saved to local storage!");
            });
        }
    });
}

function loadMethodNames() {
    var eventPositionList = [];
    var eventSizeList = [];
    var filePosition = 0;

    console.log("Getting method names...");
    ledgerDB().order("sessionTimestamp").each(function (record, recordnumber) {
        var workSessionId = record["workSessionId"];
        var workSessionDB = ledgerDB().filter({workSessionId:workSessionId});

        workSessionDB.order("eventId").each(function (record, recordnumber) {
            if (methodNameMap[record["eventMethodId"]] == null) {
                eventSizeList.push(record["eventSize"]);
                eventPositionList.push(filePosition);
                methodNameMap[record["eventMethodId"]] = true;
            }
            filePosition += record["eventSize"];
        });
    });

    if (eventPositionList.length > 0) {
        console.log("Downloading new method names...");
        $.getJSON(host + "/data/event/?event_position=" + eventPositionList.toString() + "&event_size=" + eventSizeList.toString(),
            function(result) {
                $.each(result, function(i, event){
                    methodNameMap[event["eventMethodId"]] = removePackageAndClassName(event["eventMethod"]);
                    console.log("Added method name: " + methodNameMap[event["eventMethodId"]]);
                });
            }).always(function(result) {
                console.log("Ledger loaded!");
                ledgerLoaded();
            });
    } else {
        console.log("No new method names...");
    }
}

function removePackageAndClassName(fullyQuantifiedMethodName) {
    var methodNameArr = fullyQuantifiedMethodName.split(".");
    return methodNameArr[methodNameArr.length - 1];
}