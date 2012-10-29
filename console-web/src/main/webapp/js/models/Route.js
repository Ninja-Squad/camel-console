define(['backbone'], function(Backbone) {
    var Route = Backbone.Model.extend({
        defaults: {
            name: 'unnamed',
            uri: '',
            messageCount: 0,
            successCount: 0,
            failureCount: 0,
            successRate: 0,
            averageTime: 0,
            minimumTime: 0,
            maximumTime: 0
        },
        initialize: function() {
            var successRate = 0;
            if (this.get('messageCount') != 0) {
                successRate = Math.floor(this.get('successCount') * 100 / this.get('messageCount'))
            }
            this.set('successRate', successRate);
        }
    });
    
    return Route;
});