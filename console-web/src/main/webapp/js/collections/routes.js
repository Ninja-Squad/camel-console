define(['backbone', 'models/route'], function (Backbone, Route) {

    var Routes = Backbone.Collection.extend({

        // Reference to this collection's model.
        model:Route,
        url:'api/route'

    });
    return Routes;
});
