define(['backbone'], function(Backbone) {
    var Statistic = Backbone.Model.extend({
        defaults: {
            range: 0,
            failed: 0,
            completed: 0,
            min: 0,
            max: 0,
            average: 0
        }
    });
    
    return Statistic;
});