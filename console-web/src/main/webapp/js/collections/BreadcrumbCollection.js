define(['backbone', 'models/Breadcrumb'], function (Backbone, Breadcrumb) {
    var HOME = {path: '', text: 'Home'};
    
    var BreadcrumbCollection = Backbone.Collection.extend({
        model: Breadcrumb,
        home: function() {
            this.reset([HOME]);
        },
        route: function(routeId) {
            this.reset([HOME, {path: 'routeId', text: 'Route ' + routeId}]);
        }
    });

    return BreadcrumbCollection;
});