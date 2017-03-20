function getParameter(parameterName) {
    if (window.requestParameters === undefined) {
        window.requestParameters = {};
        var queryString = window.location.search.substring(1);

        if (queryString.length > 0) {
            var i, pairs = queryString.split('&');

            for (i = 0; i < pairs.length; i++) {
                var pair = pairs[i].split('=');
                var key = pair[0].toLowerCase();
                var value = decodeURIComponent(pair[1].replace(/\+/g, " "));

                if (window.requestParameters[key]) {
                    var tempValue = window.requestParameters[key];

                    if (typeof tempValue === 'string') {
                        window.requestParameters[key] = [];
                        window.requestParameters[key].push(tempValue);
                    }

                    window.requestParameters[key].push(value);
                } else {
                    window.requestParameters[key] = value;
                }
            }
        }
    }

    return window.requestParameters[parameterName.toLowerCase()];
};