define([
], function () {
    var Server = {
        statsPerElementAndTimeUnit: function(elementId, timeUnit, options) {
            var params = {};
            if (options.from && options.to) {
            	params = {from: options.from, to: options.to};
            }
        	$.getJSON('/api/statistic/' + elementId + '/per/' + timeUnit, params, function(data) {
                options.callback(data);
            });
        },
        
        aggregatedStatsPerElement: function(elementId, options) {
            var params = {};
            if (options.from && options.to) {
            	params = {from: options.from, to: options.to};
            }
            else {
            	params = {from: 0, to: (new Date()).getTime() + 1000000};
            }
        	$.getJSON('/api/statistic/' + elementId + '/aggregated', params, function(data) {
                options.callback(data);
            });
        },
    };
    
    return Server;
});
