define([ 'underscore', 'backbone', 'collections/routes', 'hbs!templates/routes'
], function (_, Backbone, Routes, routesTemplate) {
    var RoutesView = Backbone.View.extend({

        initialize:function () {
            // Define view template
            this.template = routesTemplate;
            // Initialize the collection
            this.collection = new Routes();
            // Render the view when the collection is retrieved from the server
            this.collection.on('reset', this.render, this)
            // Request unpaginated URL
            this.collection.fetch({ data:{ page:'no'} });
        },

        events:{
            'click .route':'route'
        },

        route: function(route){
            console.log("route", route.currentTarget.id);
            Backbone.history.navigate("route/" + route.currentTarget.id, true);
        }

    });

    /**
     * percent return a percentage based on two numbers
     *
     * Usage: class='{{sprintf "Welcome %s !" username}}'
     */
    Handlebars.registerHelper('percent', function (value1, value2) {
        return value1 / value2 * 100;
    });

    return RoutesView;
});
