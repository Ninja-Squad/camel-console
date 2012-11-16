define(['backbone', 'utils/server'], function (Backbone, Server) {
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
        initialize: function () {
        }
    });
    return Route;
});