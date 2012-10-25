define([ 'underscore', 'backbone', 'flot', 'flot-selection','utils/server', 'collections/routes', 'hbs!templates/routes'
], function (_, Backbone, Flot, FlotSelection, Server, Routes, routesTemplate) {
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
            // Loading messages timeline
            // TODO
            Server.messagesPerSecond(this.renderGraph)
        },

        events:{
            'click .route':'route'
        },

        route:function (route) {
            console.log("route", route.currentTarget.id);
            Backbone.history.navigate("route/" + route.currentTarget.id, true);
        },

        renderGraph:function (data) {
            data = JSON.parse(data);
            data = [{ data: data, label: "Messages", color: "#5EB95E" }];
            var options = {
                xaxis:{ mode:"time", tickLength:5 },
                selection:{ mode:"x" },
                grid: {backgroundColor: { colors: ["#fff", "#eee"] }, clickable:true, hoverable:true},
                lines:{ show:true, fill:true }
            };
            var plot = $.plot(this.$("#placeholder"), data, options);
            var overview = $.plot(this.$("#overview"), data, {
                series:{
                    lines:{ show:true, lineWidth:1, steps:true },
                    shadowSize:0
                },
                xaxis:{ ticks:[], mode:"time" },
                yaxis:{ ticks:[], min:0, autoscaleMargin:0.1 },
                selection:{ mode:"x" }
            });

            // now connect the two
            var that = this;
            this.$("#placeholder").bind("plotselected", function (event, ranges) {
                // do the zooming
                plot = $.plot(that.$("#placeholder"), data,
                    $.extend(true, {}, options, {
                        xaxis:{ min:ranges.xaxis.from, max:ranges.xaxis.to }
                    }));

                // don't fire event on the overview to prevent eternal loop
                overview.setSelection(ranges, true);
            });
            this.$("#overview").bind("plotselected", function (event, ranges) {
                plot.setSelection(ranges);
            });
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
})
;
