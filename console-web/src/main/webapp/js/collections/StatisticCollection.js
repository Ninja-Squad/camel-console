define(['backbone', 'models/Statistic', 'utils/TimeUnit', 'utils/server'], function (Backbone, Statistic, TimeUnit, Server) {
    var StatisticCollection = Backbone.Collection.extend({
        model:Statistic,
        url:function () {
            return '/api/statistic/' + this.id + '/per/' + this.timeUnit;
        },
        initialize:function (options) {
            this.id = options.id;
            this.timeUnit = options.timeUnit;
        },
        getTimeSerie:function (attribute) {
            var result = this.map(function (model) {
                return [model.get('range'), model.get(attribute)];
            });
            return result;
        },
        changeRange: function(from, to) {
        	var timeUnit = TimeUnit.forRange(from, to);
        	var that = this;
        	Server.statsPerElementAndTimeUnitAndRange('overall', timeUnit.name, from, to, function (data) {
                data = JSON.parse(data);
                var elements = [];
            	data.forEach(function (elem) {
                    var stat = new Statistic({'range':elem[0], 'failed':elem[1], 'completed':elem[2],
                        'min':elem[3], 'max':elem[4], 'average':elem[5]});
                    elements.push(stat.toJSON());
                });
            	that.reset(elements);
            });
        }
    });
    return StatisticCollection;
});