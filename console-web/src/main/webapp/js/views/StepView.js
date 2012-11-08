define(['underscore',
    'backbone',
    'd3',
    'models/Route'], function (_, Backbone) {
    var StepView = Backbone.View.extend({
        initialize:function () {
            var res;
            var that = this;
            _.each(Object.keys(this.model.get('steps')), function (step) {
                current = {routeId:step};//, uri:that.model.get('steps')[step]};
                if (res === undefined) {
                    res = current;
                } else {
                    if (res.children === undefined) {
                        res.children = [current];
                    } else {
                        var last = res.children
                        while (last.children != undefined) {
                            last = last.children;
                        }
                        last.children = [current];
                    }
                }
            });
            this.dataset = res;
            console.log('dataset', this.dataset);
        },
        render:function (event) {
            var vis = d3.select("#steps").append("svg:svg")
                .attr("width", 400)
                .attr("height", 300)
                .append("svg:g")
                .attr("transform", "translate(40, 0)"); // shift everything to the right

            // Create a tree "canvas"
            var tree = d3.layout.tree()
                .size([300, 150]);

            var diagonal = d3.svg.diagonal()
                // change x and y (for the left to right tree)
                .projection(function (d) {
                    return [d.y, d.x];
                });

            // Preparing the data for the tree layout, convert data into an array of nodes
            var nodes = tree.nodes(this.dataset);
            // Create an array with all the links
            var links = tree.links(nodes);

            console.log(nodes)
            console.log(links)

            var link = vis.selectAll("pathlink")
                .data(links)
                .enter().append("svg:path")
                .attr("class", "link")
                .attr("d", diagonal)

            var node = vis.selectAll("g.node")
                .data(nodes)
                .enter().append("svg:g")
                .attr("transform", function (d) {
                    return "translate(" + d.y + "," + d.x + ")";
                })

            // Add the dot at every node
            node.append("svg:circle")
                .attr("r", 3.5);

            // place the name atribute left or right depending if children
            node.append("svg:text")
                .attr("dx", function (d) {
                    return d.children ? -8 : 8;
                })
                .attr("dy", 3)
                .attr("text-anchor", function (d) {
                    return d.children ? "end" : "start";
                })
                .text(function (d) {
                    return d.routeId;
                })
        }
    });
    return StepView;
});