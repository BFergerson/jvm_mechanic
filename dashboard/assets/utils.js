function getParameter (parameterName) {
  if (window.requestParameters === undefined) {
    window.requestParameters = {}
    var queryString = window.location.search.substring(1)

    if (queryString.length > 0) {
      var i, pairs = queryString.split('&')

      for (i = 0; i < pairs.length; i++) {
        var pair = pairs[i].split('=')
        var key = pair[0].toLowerCase()
        var value = decodeURIComponent(pair[1].replace(/\+/g, ' '))

        if (window.requestParameters[key]) {
          var tempValue = window.requestParameters[key]

          if (typeof tempValue === 'string') {
            window.requestParameters[key] = []
            window.requestParameters[key].push(tempValue)
          }

          window.requestParameters[key].push(value)
        } else {
          window.requestParameters[key] = value
        }
      }
    }
  }

  return window.requestParameters[parameterName.toLowerCase()]
}

function getClassMethodParamList (classAndMethodName) {
  if (!classAndMethodName || !classAndMethodName.includes('.')) return classAndMethodName
  var methodNameArr = classAndMethodName.split('.')
  var className = methodNameArr[0]
  var methodName = methodNameArr[1].substring(0, methodNameArr[1].indexOf("("))
  var params = methodNameArr[1].substring(methodNameArr[1].indexOf("(") + 1, methodNameArr[1].indexOf(")"))
  return [className, methodName, params]
}

function removePackageName (fullyQuantifiedMethodName) {
  if (!fullyQuantifiedMethodName || !fullyQuantifiedMethodName.includes('.')) return fullyQuantifiedMethodName
  var methodNameArr = fullyQuantifiedMethodName.split('.')
  return methodNameArr[methodNameArr.length - 2] + '.' + methodNameArr[methodNameArr.length - 1]
}

function removePackageAndClassName (fullyQuantifiedMethodName) {
  if (!fullyQuantifiedMethodName || !fullyQuantifiedMethodName.includes('.')) return fullyQuantifiedMethodName
  var methodNameArr = fullyQuantifiedMethodName.split('.')
  return methodNameArr[methodNameArr.length - 1]
}

function getRandomColor () {
  var letters = '0123456789ABCDEF'.split('')
  var color = '#'
  for (var i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)]
  }
  return color
}

function getPrettyTime (millisecondCount) {
  var duration = moment.duration(millisecondCount)
  var hours = duration.asHours()
  var minutes = duration.asMinutes()
  var seconds = duration.asSeconds()
  var milliseconds = duration.asMilliseconds()

  if (hours > 1) {
    return roundNumber(hours, 2) + ' hours'
  } else if (minutes > 1) {
    return roundNumber(minutes, 2) + ' minutes'
  } else if (seconds > 1) {
    return roundNumber(seconds, 2) + 's'
  } else {
    return milliseconds + 'ms'
  }
}

function roundNumber (num, scale) {
  if (!('' + num).includes('e')) {
    return +(Math.round(num + 'e+' + scale) + 'e-' + scale)
  } else {
    var arr = ('' + num).split('e')
    var sig = ''
    if (+arr[1] + scale > 0) {
      sig = '+'
    }
    return +(Math.round(+arr[0] + 'e' + sig + (+arr[1] + scale)) + 'e-' + scale)
  }
}

function humanFileSize (bytes) {
  var si = false
  var thresh = si ? 1000 : 1024
  if (Math.abs(bytes) < thresh) {
    return bytes + ' B'
  }
  var units = si
    ? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
    : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB']
  var u = -1
  do {
    bytes /= thresh
    ++u
  } while (Math.abs(bytes) >= thresh && u < units.length - 1)
  return bytes.toFixed(2) + ' ' + units[u]
}