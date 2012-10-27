define(['backbone'], function(Backbone) {
    var Statistics = Backbone.Model.extend({
        defaults: {
            successes: [],
            failures: [],
            averageTimes: [],
            minimumTimes: [],
            maximumTimes: []
        }
    });
    
    return Statistics;
});