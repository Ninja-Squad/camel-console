define(['backbone', 'views/routesView', 'backbone-queryparams'], function (Backbone, RoutesView) {
    var AppRouter = Backbone.Router.extend({

        initialize:function () {
            Backbone.history.start({ pushState:true});
        },

        routes:{
            '':'appRoutes'
        },

        appRoutes:function () {
            new RoutesView({root:$('#main')});
        }

    });

    return AppRouter;

});