define(['backbone', 'models/Statistic'], function (Backbone, Statistic) {
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
        }
    });
    return StatisticCollection;
});