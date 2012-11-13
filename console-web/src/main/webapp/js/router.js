define(['backbone',
        'views/GraphView', 
        'views/RouteTableView', 
        'views/StepView',
        'models/Statistic', 
        'collections/StatisticCollection',
        'models/Route', 
        'collections/RouteCollection',
        'utils/server',
        'utils/TimeUnit',
        'collections/BreadcrumbCollection',
        'views/BreadcrumbsView',
        'backbone-queryparams'
], function (Backbone, GraphView, RouteTableView, StepView, Statistic, StatisticCollection, Route, RouteCollection, Server) {
    var AppRouter = Backbone.Router.extend({

        initialize:function () {
            this.routeCollection = new RouteCollection();
            this.breadcrumbCollection = new BreadcrumbCollection();
            this.breadcrumbsView = new BreadcrumbsView({collection: this.breadcrumbCollection, el: '#breadcrumbs'});
            Backbone.history.start({ pushState:true});
        },

        routes:{
            '':'appRoutes',
            'console':'appRoutes',
            'console/:id':'appRoute'
        },

        appRoutes:function () {
            var stats = new StatisticCollection();
            Server.statsPerElementAndTimeUnit('overall', TimeUnit.hour.name, function (data) {
                data = JSON.parse(data);
                data.forEach(function (elem) {
                    var stat = new Statistic({'range':elem[0], 'failed':elem[1], 'completed':elem[2],
                        'min':elem[3], 'max':elem[4], 'average':elem[5]});
                    stats.add(stat);
                });
                new GraphView({collection:stats, el:'#stats', timeUnit:TimeUnit.hour}).render();
            });
            this.routeCollection.fetch();
            new RouteTableView({collection:this.routeCollection, el:'#routes'}).render();
            this.breadcrumbCollection.home();
            
            // hack to remove the step view. TODO find a better way to do that
            $('#steps').empty();
        },

        appRoute:function (id) {
            console.log("route detail", id);
            var stats = new StatisticCollection();
            Server.statsPerElementAndTimeUnit(id, TimeUnit.hour.name, function (data) {
                data = JSON.parse(data);
                data.forEach(function (elem) {
                    var stat = new Statistic({'range':elem[0], 'failed':elem[1], 'completed':elem[2],
                        'min':elem[3], 'max':elem[4], 'average':elem[5]});
                    stats.add(stat);
                })
                new GraphView({collection:stats, el:'#stats', timeUnit:TimeUnit.hour}).render();
            });
            var that = this;
            console.log("route detail", id);
            this.routeCollection.fetch({success:function (collection) {
                console.log("all routes", collection);
                that.routeCollection = collection;
                var route = that.routeCollection.find(function (model) {
                    return model.get('routeId') == id;
                });
                that.routeCollection = new RouteCollection();
                _.each(Object.keys(route.get('steps')), function (step) {
                    that.routeCollection.add(new Route({routeId:step, uri:route.get('steps')[step]}));
                });
                new RouteTableView({collection:that.routeCollection, el:'#routes'}).render();
                new StepView({model:route, el:'#steps'}).render();
            }});
            this.breadcrumbCollection.route(id);
        }

    });

    return AppRouter;

});