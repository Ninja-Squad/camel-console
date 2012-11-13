define(['underscore', 
        'backbone',
        'hbs!templates/breadcrumbs',
        ], function (_, Backbone, breadcrumbsTemplate) {
    var BreadcrumbsView = Backbone.View.extend({
        initialize: function() {
            this.template = breadcrumbsTemplate;
            this.collection.on('reset', this.render, this);
        },
        render: function(event) {
            this.$el.html(this.template({breadcrumbs: this.collection.toJSON()}));
            return this;
        },
        events: {
            'click a': 'navigate'
        },
        navigate: function(event) {
            Backbone.history.navigate($(event.target).attr('data-path'), true);
        }
    });
    
    return BreadcrumbsView;
});