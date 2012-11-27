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
        initialize:function () {
            // fetch route stats
            this.fetchStats();
        },
        fetchStats:function () {
            var that = this;
            var options = {silent:true};
            var stats = new StatisticCollection({id:that.get('routeId'), timeUnit:'year'});
            stats.fetch({success:function () {
                var stat = stats.at(0);
                console.log(stat.get('completed'));
                that.set('failureCount', stat.get('failed'), options);
                that.set('successCount', stat.get('completed'), options);
                that.set('minimumTime', stat.get('min'), options);
                that.set('maximumTime', stat.get('max'), options);
                that.set('averageTime', stat.get('average'), options);
                that.set('messageCount', stat.get('failed') + stat.get('completed'), options);
                var successRate = 0;
                if (that.get('messageCount') != 0) {
                    successRate = Math.floor(that.get('successCount') * 100 / that.get('messageCount'));
                }
                that.set('successRate', successRate, options);
                that.change();
            }})
        }
    });
    return Route;
});