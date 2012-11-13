define(['backbone', 'models/Statistic'], function (Backbone, Statistic) {
    var StatisticCollection = Backbone.Collection.extend({
        model:Statistic,
        url:'api/statistic',
        initialize:function () {
            timeUnit: 'hour';
        },
        getTimeSerie:function (attribute) {
            var result = this.map(function(model){
                return [model.get('range'), model.get(attribute)];
            });
            return result;
        }
    });
    return StatisticCollection;
});