function updatePlaybackRange (startTime, endTime) {
  //make sure we have all sessions in playback range
  $.getJSON(host + '/playback/data/session/time/?start_time=' + moment(startTime, 'x').valueOf() + '&end_time=' + moment(endTime, 'x').valueOf(), function (result) {
    var dateSlider = document.getElementById('slider-date')
    var momentFormatter = {
      to: function (value) {
        return moment(value).format('hh:mm:ss')
      },
      from: function (value) {
        return moment(value, 'hh:mm:ss').valueOf()
      }
    }

    var stepCount = 60 * 1000
    var playbackMinutes = moment.duration(moment(result.lastActualEvent).diff(result.firstActualEvent)).asMinutes()
    if (playbackMinutes < 30) {
      stepCount /= 60 //step count: seconds
    }

    var createSlider = false
    if (dateSlider.noUiSlider) {
      if (startTime < dateSlider.noUiSlider.options.range.min || endTime > dateSlider.noUiSlider.options.range.max) {
        dateSlider.noUiSlider.destroy()
        createSlider = true
      }
    } else {
      createSlider = true
    }

    if (createSlider) {
      noUiSlider.create(dateSlider, {
        range: {
          min: result.firstActualEvent,
          max: result.lastActualEvent
        },
        step: stepCount,
        start: [startTime, endTime],
        behaviour: 'drag',
        connect: true,
        tooltips: [momentFormatter, momentFormatter],
        format: wNumb({
          decimals: 0
        })
      })

      dateSlider.noUiSlider.on('change', function (values, handle) {
        var start = new Number(values[0])
        var end = new Number(values[1])
        updatePlaybackRange(start, end)
      })
    }

    //update charts
    updatePlaybackCharts(startTime, endTime, result)
  })
}