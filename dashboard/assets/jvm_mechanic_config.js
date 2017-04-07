//config
var host = 'http://localhost:9000'

loadConfigSettings()
//update config every 30 seconds
setInterval(function () {
  loadConfigSettings()
}, 30000)

function loadConfigSettings () {
  console.log('Downloading jvm_mechanic configuration settings...')

  $.getJSON(host + '/config', function (result) {
    console.log('Displaying jvm_mechanic configuration settings!')

    var journalEntrySize = result.journalEntrySize
    $('#sessionSampleAccuracy').text(result.sessionSampleAccuracy + '%')
    $('#playbackModeEnabled').text(result.playbackModeEnabled)

    if (result.playbackModeEnabled === false) {
      $('#playbackModeEnabled').css('color', 'red')

      if (monitorMode === 'playback') {
        //you shouldn't be here!
        window.location.replace('index.html?mode=live')
      }
    } else {
      $('#playbackModeEnabled').css('color', 'green')
      $('#dropdownMenuButton').removeClass('disabled')
    }

    $('#ledgerFileLocation').text(result.ledgerFileLocation)
    $('#ledgerEntryCount').text(formatSizeUnits(result.ledgerFileSize) + ' (' + (result.ledgerFileSize / result.journalEntrySize) + ' events)')
    $('#dataFileLocation').text(result.dataFileLocation)
    $('#dataEntryCount').text(formatSizeUnits(result.dataFileSize) + ' (' + (result.ledgerFileSize / result.journalEntrySize) + ' events)')
    $('#ledgerFileSize').text(result.ledgerFileSize)
    $('#dataFileSize').text(result.dataFileSize)
    $('#gcFileLocation').text(result.gcFileLocation)
    $('#gcFileSize').text(formatSizeUnits(result.gcFileSize))

    var monitorMethodCount = 0
    if (result.methodNameMap) {
      Object.keys(result.methodNameMap).forEach(function (methodId) {
        methodNameMap[methodId] = result.methodNameMap[methodId]
        monitorMethodCount++
      })
    }
    $('#monitorMethodCount').text(monitorMethodCount)
  }).fail(function (error) {
      if (monitorMode === 'playback') {
        //you shouldn't be here!
        window.location.replace('index.html?mode=live')
      }
  }).always(function (result) {
    //todo: anything?
  })
}

function formatSizeUnits (bytes) {
  if (bytes >= 1000000000) {bytes = (bytes / 1000000000).toFixed(2) + ' GB'}
  else if (bytes >= 1000000) {bytes = (bytes / 1000000).toFixed(2) + ' MB'}
  else if (bytes >= 1000) {bytes = (bytes / 1000).toFixed(2) + ' KB'}
  else if (bytes > 1) {bytes = bytes + ' bytes'}
  else if (bytes === 1) {bytes = bytes + ' byte'}
  else {bytes = '0 bytes'}
  return bytes
}