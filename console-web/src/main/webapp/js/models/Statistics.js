define(['backbone', 'utils/TimeUnit', "collections/StatisticCollection"], function(Backbone, TimeUnit, StatisticCollection) {
    var Statistics = Backbone.Model.extend({
        defaults: {
            timeUnit: TimeUnit.day,
            statisticCollection: new StatisticCollection()
        }
    });
    
    return Statistics;
});