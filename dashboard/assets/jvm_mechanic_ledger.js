//config
var host = "http://localhost:9000";
var ledgerPosition = 0;
var monitorMode = getParameter('mode');
if (!monitorMode) {
    monitorMode = "Live";
}

//init dbs
var seenMethodIdMap = {};
var methodNameMap = {};

//load local storage
var desiredCapacity = 50 * 1024 * 1024; // 50MB
var storage = new LargeLocalStorage({
    size: desiredCapacity
});
storage.initialized.then(function() {
    console.log("Local storage size: " + storage.size);
    console.log("Local storage capacity: " + storage.getCapacity() + " bytes");
    loadLedgerUpdates(true);
}, function() {
    console.log('denied');
    //todo: fail in some spectacular way
});

function loadLedgerUpdates() {
    loadLedgerUpdates(false);
}

function loadLedgerUpdates(initialLoad) {
    if (ledgerPosition == 0 && monitorMode == 'Live') {
        //get current position, start there; live mode
        $.getJSON(host + "/config", function(result) {
            console.log("Getting current ledger position to begin there for live mode!");
            var journalEntrySize = result.journalEntrySize;
            ledgerPosition = (result.ledgerFileSize / result.journalEntrySize);
            console.log("Starting with ledger position: " + ledgerPosition);

            sendLedgerRequest(initialLoad);
        }).always(function(result) {
            //todo: anything?
        });
    } else {
        //start from beginning; playback mode
        sendLedgerRequest(initialLoad);
    }
}

function sendLedgerRequest(initialLoad) {
    console.log("Checking for ledger updates...");
    var ledgerUpdated = false;
    $.getJSON(host + "/ledger/?current_ledger_size=" + ledgerPosition, function(result) {
        ledgerUpdated = true;
        console.log("Got ledger updates! Size: " + result.length);
        $.each(result, function(i, entry) {
            addRecordedEvent(entry);

            if (entry.eventMethodId) {
                seenMethodIdMap[entry.eventMethodId] = true;
            }
        });
    }).always(function(result) {
        if (!ledgerUpdated && !initialLoad) {
            console.log("No ledger updates found! Current position: " + ledgerPosition);
        } else if (initialLoad) {
            console.log("Ledger loaded!");
            ledgerLoaded();
        }

        if (!initialLoad && ledgerUpdated) {
            ledgerUpdated0();
        }
    });
}

function ledgerUpdated0() {
    ledgerUpdated();
}