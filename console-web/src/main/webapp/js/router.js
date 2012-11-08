define(['backbone', 'views/GraphView', 'views/RouteTableView',
    'models/Statistic', 'collections/StatisticCollection',
    'models/Route', 'collections/RouteCollection',
    'utils/Server',
    'backbone-queryparams'
], function (Backbone, GraphView, RouteTableView, Statistic, StatisticCollection, Route, RouteCollection, Server) {
    var AppRouter = Backbone.Router.extend({

        initialize:function () {
            Backbone.history.start({ pushState:true});
        },

        routes:{
            '':'appRoutes',
            'route/:id':'appRoute'
        },

        appRoutes:function () {
            var stats = new StatisticCollection();
            Server.statsPerElementAndTimeUnit('overall', 'hour', function (data) {
                data = JSON.parse(data);
                data.forEach(function (elem) {
                    var stat = new Statistic({'range':elem[0], 'failed':elem[1], 'completed':elem[2],
                        'min':elem[3], 'max':elem[4], 'average':elem[5]});
                    stats.add(stat);
                })
                new GraphView({collection:stats, el:'#stats'}).render();
            });
            var routeCollection = new RouteCollection();
            routeCollection.fetch();
            new RouteTableView({collection:routeCollection, el:'#routes'}).render();
        },

        appRoute:function (id) {
            new RouteView({root:$('#main'), id:id});
        }

    });

    return AppRouter;

});