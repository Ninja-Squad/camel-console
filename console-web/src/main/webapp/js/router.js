define(['backbone', 'views/GraphView', 'views/RouteTableView', 'models/Statistics', 'models/Route', 'collections/RouteCollection', 'backbone-queryparams'
], function (Backbone, GraphView, RouteTableView, Statistics, Route, RouteCollection) {
    var AppRouter = Backbone.Router.extend({

        initialize:function () {
            Backbone.history.start({ pushState:true});
        },

        routes:{
            '': 'appRoutes',
            'route/:id': 'appRoute'
        },

        appRoutes: function () {
            var generateTimestamps = function() {
                var today = new Date();
                today.setUTCHours(0);
                today.setUTCMinutes(0);
                today.setUTCSeconds(0);
                today.setUTCMilliseconds(0);
                
                var result = [];
                for (var i = 0; i < 31; i++) {
                    var date = new Date();
                    date.setTime(today.getTime());
                    var offset = i - 31;
                    date.setUTCDate(date.getUTCDate() + offset);
                    result.push(date.getTime());
                }
                return result;
            };
        
            var timestamps = generateTimestamps();
            var successes = [67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67];
            var failures = [5, 2, 7, 6, 5, 0, 5, 2, 7, 6, 5, 0, 5, 2, 7, 6, 5, 0, 5, 2, 7, 6, 5, 0, 5, 2, 7, 6, 5, 0, 5, 2, 7, 6, 5, 0, 5];
            var averageTimes = [67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67, 54, 57, 61, 70, 67];
            var minimumTimes = $.map(averageTimes, function(value) {return value - 10});
            var maximumTimes = $.map(averageTimes, function(value) {return value + 100});
            
            var stats = new Statistics({successes: successes, failures: failures, averageTimes: averageTimes, minimumTimes: minimumTimes, maximumTimes: maximumTimes, timestamps: timestamps});
            var routeCollection = new RouteCollection();
            /*
            routeCollection.add(new Route({name: 'Long route name 3', messageCount: 234, successCount: 200, failureCount: 34, averageTime: 57, minimumTime: 12, maximumTime: 345}));
            routeCollection.add(new Route({name: 'Long route name 2', messageCount: 224, successCount: 200, failureCount: 24, averageTime: 47, minimumTime: 12, maximumTime: 345}));
            routeCollection.add(new Route({name: 'Long route name 1', messageCount: 244, successCount: 200, failureCount: 44, averageTime: 37, minimumTime: 12, maximumTime: 345}));
            */
            routeCollection.fetch();
            new RouteTableView({collection: routeCollection, el: '#routes'}).render();
            new GraphView({model: stats, el: '#stats'}).render();
        },

        appRoute:function (id) {
            new RouteView({root: $('#main'), id: id});
        }

    });

    return AppRouter;

});