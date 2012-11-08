define(['backbone',
    'views/GraphView', 'views/RouteTableView', 'views/StepView',
    'models/Statistic', 'collections/StatisticCollection',
    'models/Route', 'collections/RouteCollection',
    'utils/server',
    'backbone-queryparams'
], function (Backbone, GraphView, RouteTableView, StepView,
             Statistic, StatisticCollection,
             Route, RouteCollection, Server) {
    var AppRouter = Backbone.Router.extend({

        initialize:function () {
            this.routeCollection = new RouteCollection();
            Backbone.history.start({ pushState:true});
        },

        routes:{
            '':'appRoutes',
            ':id':'appRoute'
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
            this.routeCollection.fetch();
            new RouteTableView({collection:this.routeCollection, el:'#routes'}).render();
        },

        appRoute:function (id) {
            console.log("route detail", id);
            var stats = new StatisticCollection();
            Server.statsPerElementAndTimeUnit(id, 'hour', function (data) {
                data = JSON.parse(data);
                data.forEach(function (elem) {
                    var stat = new Statistic({'range':elem[0], 'failed':elem[1], 'completed':elem[2],
                        'min':elem[3], 'max':elem[4], 'average':elem[5]});
                    stats.add(stat);
                })
                new GraphView({collection:stats, el:'#stats'}).render();
            });
            var route = this.routeCollection.find(function (model) {
                return model.get('routeId') == id;
            });
            this.routeCollection = new RouteCollection();
            var that = this;
            _.each(Object.keys(route.get('steps')), function (step) {
                that.routeCollection.add(new Route({routeId:step, uri:route.get('steps')[step]}));
            });
            new RouteTableView({collection:this.routeCollection, el:'#routes'}).render();
            new StepView({model:route, el:'#steps'}).render();
        }

    });

    return AppRouter;

});