define([
], function () {
    var Server = {
        statsPerElementAndTimeUnit: function(elementId, timeUnit, callback){
            $.getJSON('/api/statistic/' + elementId + '/per/' + timeUnit, function(data){
                console.log("stats for", elementId, timeUnit, data);
                callback(data);
            });
        }
    };
    
    return Server;
});
