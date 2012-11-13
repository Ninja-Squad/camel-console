define(['backbone'], function (Backbone) {
    var Breadcrumb = Backbone.Model.extend({
        defaults:{
            path:'',
            text:'',
        }
    });
    return Breadcrumb;
});