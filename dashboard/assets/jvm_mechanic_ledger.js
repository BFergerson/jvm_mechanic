//config
var host = "http://localhost:9000";

//init dbs
var ledgerDB = TAFFY();
var sessionDB = TAFFY();
var methodNameMap = {};
var corruptEventMap = {};

//load local storage
var desiredCapacity = 50 * 1024 * 1024; // 50MB
var storage = new LargeLocalStorage({
    size: desiredCapacity
});
storage.initialized.then(function() {
    storage.getContents('ledger_data').then(function(content) {
        console.log("Checking cache for: ledger_data");
        if (content) {
            console.log("Found ledger_data in cache! Loading...");
            ledgerDB = TAFFY(content);
            ledgerDB().each(function (record, recordnumber) {
                sessionDB.merge({workSessionId:record["workSessionId"], sessionTimestamp:ledgerDB().filter({workSessionId:record["workSessionId"]}).min("eventTimestamp")}, "workSessionId");
            });
            console.log("Loaded cached data from: ledger_data! Size: " + ledgerDB().count());
        } else {
            console.log("Nothing in cache for: ledger_data");
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
            loadMethodNames(initialLoad);

            //save ledger
            storage.setContents('ledger_data', ledgerDB().stringify()).then(function() {
                console.log("Saved ledger_data to local storage!");
            });
        } else {
            //save ledger
            storage.setContents('ledger_data', ledgerDB().stringify()).then(function() {
                console.log("Saved ledger_data to local storage!");
            });
        }
    });
}

function loadMethodNames(initialLoad) {
    storage.getContents('ledger_data.method_names').then(function(content) {
        console.log("Checking cache for: ledger_data.method_names");
        if (content) {
            methodNameMap = JSON.parse(content);
            console.log("Found ledger_data.method_names in cache! Size: " + Object.keys(methodNameMap).length);
        } else {
            console.log("Nothing in cache for: ledger_data.method_names");
        }

        downloadMethodNames(initialLoad);
    });
}
function downloadMethodNames(initialLoad) {
    var eventPositionList = [];
    var eventSizeList = [];
    var filePosition = 0;
    var tmpMethodNameMap = {};

    console.log("Getting method names...");
    sessionDB().order("sessionTimestamp").each(function (record, recordnumber) {
        var workSessionId = record["workSessionId"];
        var workSessionDB = ledgerDB().filter({workSessionId:workSessionId});

        workSessionDB.order("ledgerId").each(function (record, recordnumber) {
            if (methodNameMap[record["eventMethodId"]] == null && tmpMethodNameMap[record["eventMethodId"]] == null ) {
                eventSizeList.push(record["eventSize"]);
                eventPositionList.push(filePosition);
                tmpMethodNameMap[record["eventMethodId"]] = true;
            }
            filePosition += record["eventSize"];
        });
    });

    if (eventPositionList.length > 0) {
        console.log("Downloading new method names...");
        $.getJSON(host + "/data/event/?event_position=" + eventPositionList.toString() + "&event_size=" + eventSizeList.toString(),
            function(result) {
                $.each(result, function(i, event){
                    if (event["eventType"] == "CORRUPT_EVENT") {
                        corruptEventMap[event["eventId"]] = event["workSessionId"];
                        console.log("Skipped adding method name from corrupt event!");
                    } else {
                        methodNameMap[event["eventMethodId"]] = removePackageAndClassName(event["eventMethod"]);
                        console.log("Added method name: " + methodNameMap[event["eventMethodId"]]);
                    }
                });
            }).always(function(result) {
                //save method names
                storage.setContents('ledger_data.method_names', JSON.stringify(methodNameMap)).then(function() {
                    console.log("Saved ledger_data.method_names to local storage!");
                });

                console.log("Ledger loaded!");
                ledgerLoaded();
            });
    } else {
        console.log("No new method names...");

        if (initialLoad) {
            console.log("Ledger loaded!");
            ledgerLoaded();
        }
    }
}

function removePackageAndClassName(fullyQuantifiedMethodName) {
    if (fullyQuantifiedMethodName == null || !fullyQuantifiedMethodName.includes(".")) return fullyQuantifiedMethodName;
    var methodNameArr = fullyQuantifiedMethodName.split(".");
    return methodNameArr[methodNameArr.length - 1];
}