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
            this.set('successRate', Math.floor(this.get('successCount') * 100 / this.get('messageCount')));
        }
    });
    
    return Route;
});