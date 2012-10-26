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
            var successes = [[1, 67], [2, 54], [3, 57], [4, 61], [5, 70], [6, 67], [7, 54], [8, 57], [9, 61], [10, 70], [11, 67], [12, 54], [13, 57], [14, 61], [15, 70], [16, 67], [17, 54], [18, 57], [19, 61], [20, 70], [21, 67], [22, 54], [23, 57], [24, 61], [25, 70], [26, 67], [27, 54], [28, 57], [29, 61], [30, 70], [31, 67]];
        
            var failures = [[1, 5], [2, 2], [3, 7], [4, 6], [5, 0], [6, 5], [7, 2], [8, 7], [9, 6], [10, 0], [11, 5], [12, 2], [13, 7], [14, 6], [15, 0], [16, 5], [17, 2], [18, 7], [19, 6], [20, 0], [21, 5], [22, 2], [23, 7], [24, 6], [25, 0], [26, 5], [27, 2], [28, 7], [29, 6], [30, 0], [31, 5]];
            
            var averageTimes = [[1, 67], [2, 54], [3, 57], [4, 61], [5, 70], [6, 67], [7, 54], [8, 57], [9, 61], [10, 70], [11, 67], [12, 54], [13, 57], [14, 61], [15, 70], [16, 67], [17, 54], [18, 57], [19, 61], [20, 70], [21, 67], [22, 54], [23, 57], [24, 61], [25, 70], [26, 67], [27, 54], [28, 57], [29, 61], [30, 70], [31, 67]];
            
            var minimumTimes = [];
            var maximumTimes = [];
            $.each(averageTimes, function(index, point) {
                minPoint = [point[0], point[1] - 10];
                maxPoint = [point[0], point[1] + 100];
                
                minimumTimes.push(minPoint);
                maximumTimes.push(maxPoint);
            });
        
            var stats = new Statistics({successes: successes, failures: failures, averageTimes: averageTimes, minimumTimes: minimumTimes, maximumTimes: maximumTimes});
            var routeCollection = new RouteCollection();
            routeCollection.add(new Route({name: 'Long route name 3', messageCount: 234, successCount: 200, failureCount: 34, averageTime: 57, minimumTime: 12, maximumTime: 345}));
            routeCollection.add(new Route({name: 'Long route name 2', messageCount: 224, successCount: 200, failureCount: 24, averageTime: 47, minimumTime: 12, maximumTime: 345}));
            routeCollection.add(new Route({name: 'Long route name 1', messageCount: 244, successCount: 200, failureCount: 44, averageTime: 37, minimumTime: 12, maximumTime: 345}));
            
            new RouteTableView({collection: routeCollection, el: '#routes'}).render();
            new GraphView({model: stats, el: '#stats'}).render();
        },

        appRoute:function (id) {
            new RouteView({root: $('#main'), id: id});
        }

    });

    return AppRouter;

});