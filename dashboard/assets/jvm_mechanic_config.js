//config
var host = "http://localhost:9000";

loadConfigSettings();
//update config every 30 seconds
setInterval(function() {
    loadConfigSettings();
}, 30000);


function loadConfigSettings() {
    console.log("Downloading jvm_mechanic configuration settings...");

    $.getJSON(host + "/config", function(result) {
        console.log("Displaying jvm_mechanic configuration settings!");

        var journalEntrySize = result.journalEntrySize;
        $("#sessionSampleAccuracy").text(result.sessionSampleAccuracy + "%");
        $("#ledgerFileLocation").text(result.ledgerFileLocation);
        $("#ledgerEntryCount").text((result.ledgerFileSize / result.journalEntrySize) + " (" + result.ledgerFileSize + " bytes)");
        $("#dataFileLocation").text(result.dataFileLocation);
        $("#dataEntryCount").text((result.ledgerFileSize / result.journalEntrySize) + " (" + result.dataFileSize + " bytes)");
        $("#ledgerFileSize").text(result.ledgerFileSize);
        $("#dataFileSize").text(result.dataFileSize);
        $("#gcFileLocation").text(result.gcFileLocation);
        $("#gcFileSize").text(result.gcFileSize + " bytes");
    }).always(function(result) {
        //todo: anything?
    });
}