define(['backbone', 'utils/server'], function (Backbone, Server) {
    var Route = Backbone.Model.extend({
        defaults:{
            uri:'',
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
            Server.statsPerElementAndTimeUnit(that.get('routeId'), 'year', function (data) {
                data = JSON.parse(data);
                data.forEach(function (elem) {
                    that.set('failureCount', elem[1], {silent: true});
                    that.set('successCount', elem[2], {silent: true});
                    that.set('minimumTime', elem[3], {silent: true});
                    that.set('maximumTime', elem[4], {silent: true});
                    that.set('averageTime', elem[5], {silent: true});
                    that.set('messageCount', elem[1] + elem[2], {silent: true});
                });
                var successRate = 0;
                if (that.get('messageCount') != 0) {
                    successRate = Math.floor(that.get('successCount') * 100 / that.get('messageCount'))
                }
                that.set('successRate', successRate, {silent: true});
            });
        }
    });
    return Route;
});