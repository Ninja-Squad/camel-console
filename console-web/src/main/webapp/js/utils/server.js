define([
], function () {
    var Server = {
        statsPerElementAndTimeUnit: function(elementId, timeUnit, options) {
            var params = {};
            if (options.from && options.to) {
            	params = {from: options.from, to: options.to};
            }
        	$.getJSON('/api/statistic/' + elementId + '/per/' + timeUnit, params, function(data) {
                console.log("stats for", elementId, timeUnit, data);
                options.callback(data);
            });
        },
        
    };
    
    return Server;
});
