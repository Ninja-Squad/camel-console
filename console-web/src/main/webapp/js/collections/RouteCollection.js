define(['backbone', 'models/Route'], function (Backbone, Route) {
    var RouteCollection = Backbone.Collection.extend({
        model: Route,
        initialize: function() {
            this.setSortAttribute('name');
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
            this.comparator = function(route1, route2) {
                var result = 0;
                if (route1.get(attribute) < route2.get(attribute)) {
                    result = -1;
                }
                else if (route1.get(attribute) > route2.get(attribute)) {
                    result = 1;
                }
                return (this.asc ? result : -result);
            }
        }
    });
    
    return RouteCollection;
});