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
        _accepted: function(route) {
            if (!this.routeFilter || !this.routeFilter.trim()) {
                return true;
            }
            var appliedFilter = this.routeFilter.trim().toLowerCase();
            var checkedRouteId = route.get("routeId").toLowerCase();
            return checkedRouteId.indexOf(appliedFilter) == 0;
        },
        renderTable: function() {
            this.$("thead tr").html(this.mode == 'numbers' ? this.numbersHeaderTemplate() : this.blocksHeaderTemplate());
            this.$('tbody').html('');
            this.collection.each(function(route) {
                if (this._accepted(route)) {
                    var routeView = new RouteView({model: route});
                    routeView.setMode(this.mode);
                    this.$('tbody').append(routeView.render().$el);
                }
            }, this);
            this.$("th[data-sort]").removeClass("sorted-asc").removeClass("sorted-desc");
            if (this.collection.sortAttribute) {
                this.$("th[data-sort=" + this.collection.sortAttribute + "]").addClass("sorted-" + (this.collection.asc ? "asc" : "desc"));
            }
            // hack to make tooltips disappear
            $('body > .tooltip').remove(); 
            return this;
        },
        events: {
            'click [data-id=blocks]': 'setBlocksMode',
            'click [data-id=numbers]': 'setNumbersMode',
            'click th' : 'sort',
            'change [data-id=filter]': 'filter',
            'input [data-id=filter]': 'filter',
            'click [data-id=clearFilter]': 'clearFilter'
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
        },
        filter: function(event) {
            this.routeFilter = this.$("[data-id=filter]").val().trim();
            this.$("[data-id=clearFilter]").attr("disabled", !(this.routeFilter && true));
            this.renderTable();
        },
        clearFilter: function(event) {
            this.$("[data-id=filter]").val("");
            this.filter();
        }
    });
    
    return RouteTableView;
});