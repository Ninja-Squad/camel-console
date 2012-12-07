define(['backbone', 'collections/StatisticCollection'], function (Backbone, StatisticCollection) {
    var Route = Backbone.Model.extend({
        defaults:{
            uri:'',
            routeId:'',
            messageCount:0,
            successCount:0,
            failureCount:0,
            successRate:0,
            averageTime:0,
            minimumTime:0,
            maximumTime:0
        },
        // TODO JB: j'avais carrément enlevé le corps de initialize et la fonction fetchStats
        initialize: function() {
        }
    });
    return Route;
});