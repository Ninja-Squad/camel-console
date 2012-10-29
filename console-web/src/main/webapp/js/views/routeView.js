define([ 'underscore', 'backbone', 'models/route', 'hbs!templates/route'
], function (_, Backbone, RouteModel, routeTemplate) {
    var RouteView = Backbone.View.extend({

        initialize:function (options) {
            this.$root = options.root;
            // Define view template
            this.template = routeTemplate;
            // Initialize the model
            this.model = new RouteModel();
            this.model.id = options.id;
            // Request model
            this.model.fetch({
                success: this.render.bind(this)
            });
        }

    });

    return RouteView;
});
