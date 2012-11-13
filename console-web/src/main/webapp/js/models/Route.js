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
        initialize:function () {
            // fetch route stats
            this.fetchStats();
        },
        fetchStats:function () {
            var that = this;
            var options = {silent: true};
            Server.statsPerElementAndTimeUnit(that.get('routeId'), 'year', function (data) {
                data = JSON.parse(data);
                // the result is an array containing a single array
                data.forEach(function (elem) {
                    that.set('failureCount', elem[1], options);
                    that.set('successCount', elem[2], options);
                    that.set('minimumTime', elem[3], options);
                    that.set('maximumTime', elem[4], options);
                    that.set('averageTime', elem[5], options);
                    that.set('messageCount', elem[1] + elem[2], options);
                });
                var successRate = 0;
                if (that.get('messageCount') != 0) {
                    successRate = Math.floor(that.get('successCount') * 100 / that.get('messageCount'));
                }
                that.set('successRate', successRate, options);
                that.change();
            });
        }
    });
    return Route;
});