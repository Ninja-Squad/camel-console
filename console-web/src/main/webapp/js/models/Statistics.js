define(['backbone'], function(Backbone) {
    var Statistics = Backbone.Model.extend({
        defaults: {
            successes: [],
            failures: [],
            averageTimes: [],
            minimumTimes: [],
            maximumTimes: [],
            timestamps: [],
        },
        getTimeSerie: function(attribute) {
            var data = this.get(attribute);
            var result= [];
            $.each(this.get('timestamps'), function(index, value) {
                result.push([value, data[index]]);
            });
            return result;
        }
    });
    
    return Statistics;
});