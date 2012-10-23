define(['backbone', 'views/routesView', 'views/routeView','backbone-queryparams'
], function (Backbone, RoutesView, RouteView) {
    var AppRouter = Backbone.Router.extend({

        initialize:function () {
            Backbone.history.start({ pushState:true});
        },

        routes:{
            '':'appRoutes',
            'route/:id':'appRoute'
        },

        appRoutes:function () {
            new RoutesView({root:$('#main')});
        },

        appRoute:function (id) {
            new RouteView({root:$('#main'), id:id});
        }

    });

    return AppRouter;

});