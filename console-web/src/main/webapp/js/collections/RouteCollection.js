define(['backbone', 'models/Route'], function (Backbone, Route) {
    var RouteCollection = Backbone.Collection.extend({
        model: Route,
        url: '/api/route?size=100',
        initialize: function() {
            this.setSortAttribute('routeId');
            this.asc = true;
        },
        setSortAttribute: function(attribute) {
            if (attribute == this.sortAttribute) {
                this.asc = !this.asc;
            }
            else {
                this.asc = true;
            }
            this.sortAttribute = attribute;
            this.comparator = function (route1, route2) {
                var result = 0;
                if (route1.get(attribute) < route2.get(attribute)) {
                    result = -1;
                }
                else if (route1.get(attribute) > route2.get(attribute)) {
                    result = 1;
                }
                return (this.asc ? result : -result);
            };
        },
        parse: function(response) {
            return $.map(response.content, function (item) {
                return {routeId:item.routeId, uri:item.uri, steps:item.steps};
            });
        }
    });

    return RouteCollection;
});