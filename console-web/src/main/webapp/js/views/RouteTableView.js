define(['underscore', 
        'backbone',
        'views/RouteView',
        'hbs!templates/routes',
        'hbs!templates/routeTableBlocksHeader',
        'hbs!templates/routeTableNumbersHeader',
        'bootstrap'], function (_, Backbone, RouteView, routesTemplate, blocksHeaderTemplate, numbersHeaderTemplate) {
    var RouteTableView = Backbone.View.extend({
        initialize: function() {
            this.template = routesTemplate;
            this.blocksHeaderTemplate = blocksHeaderTemplate;
            this.numbersHeaderTemplate = numbersHeaderTemplate;
            this.mode = 'blocks';
            this.collection.on('reset', this.renderTable, this);
        },
        render: function(event) {
            this.$el.html(this.template());
            this.$('.btn-group').button();
            this.$('[data-id=' + this.mode + ']').button('toggle');
            this.renderTable();
            return this;
        },
        renderTable: function() {
            this.$("thead tr").html(this.mode == 'numbers' ? this.numbersHeaderTemplate() : this.blocksHeaderTemplate());
            this.$('tbody').html('');
            this.collection.each(function(route) {
                var routeView = new RouteView({model: route});
                routeView.setMode(this.mode);
                this.$('tbody').append(routeView.render().$el);
            }, this);
            this.$("th[data-sort]").removeClass("sorted-asc").removeClass("sorted-desc");
            if (this.collection.sortAttribute) {
                this.$("th[data-sort=" + this.collection.sortAttribute + "]").addClass("sorted-" + (this.collection.asc ? "asc" : "desc"));
            }
            return this;
        },
        events: {
            'click [data-id=blocks]': 'setBlocksMode',
            'click [data-id=numbers]': 'setNumbersMode',
            'click th' : 'sort'
        },
        setBlocksMode: function(event) {
            this.mode = 'blocks';
            this.renderTable();
        },
        setNumbersMode: function(event) {
            this.mode = 'numbers';
            this.renderTable();
        },
        sort: function(event) {
            var sort = $(event.target).attr('data-sort');
            if (sort) {
                this.collection.setSortAttribute(sort);
                this.collection.sort();
            }
        }
    });
    
    return RouteTableView;
});