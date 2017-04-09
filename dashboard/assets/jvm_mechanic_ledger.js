//config
var host = 'http://localhost:9000'
var ledgerPosition = 0

//init dbs
var seenMethodIdMap = {}
var methodNameMap = {}

$(document).ready(function () {
  if (monitorMode !== 'playback') {
      loadLedgerUpdates(true)
  } else {
      ledgerLoaded()
  }
})

function loadLedgerUpdates () {
  loadLedgerUpdates(false)
}

function loadLedgerUpdates (initialLoad) {
  if (ledgerPosition === 0 && monitorMode === 'live') {
    //get current position, start there; live mode
    $.getJSON(host + '/config', function (result) {
      console.log('Getting current ledger position to begin there for live mode!')
      var journalEntrySize = result.journalEntrySize
      ledgerPosition = (result.ledgerFileSize / result.journalEntrySize)
      console.log('Starting with ledger position: ' + ledgerPosition)

      sendLedgerRequest(initialLoad)
    }).fail(function (error) {
        //start with 0 position
        sendLedgerRequest(initialLoad)
    }).always(function (result) {
      //todo: anything?
    })
  } else {
    //start from beginning; playback mode
    sendLedgerRequest(initialLoad)
  }
}

function sendLedgerRequest (initialLoad) {
  console.log('Checking for ledger updates...')
  var ledgerUpdated = false
  $.getJSON(host + '/ledger/?current_ledger_size=' + ledgerPosition, function (result) {
    ledgerUpdated = true
    console.log('Got ledger updates! Size: ' + result.journalEntryList.length)
    console.log("Ledger difference: " + (result.maximumLedgerPosition - (ledgerPosition + result.journalEntryList.length)))
    $.each(result.journalEntryList, function (i, entry) {
      ledgerPosition++
      addRecordedEvent(entry)

      if (entry.eventMethodId) {
        seenMethodIdMap[entry.eventMethodId] = true
      }
    })
  }).always(function (result) {
    if (!ledgerUpdated && !initialLoad) {
      console.log('No ledger updates found! Current position: ' + ledgerPosition)
    } else if (initialLoad) {
      console.log('Ledger loaded!')
      ledgerLoaded()
    }

    if (!initialLoad && ledgerUpdated) {
      ledgerUpdated0()
    }
  })
}

function ledgerUpdated0 () {
  ledgerUpdated()
}