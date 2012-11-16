define(['backbone',
        'views/GraphView', 
        'views/RouteTableView', 
        'views/StepView',
        'models/Statistic', 
        'collections/StatisticCollection',
        'models/Statistics', 
        'models/Route', 
        'collections/RouteCollection',
        'utils/server',
        'utils/TimeUnit',
        'collections/BreadcrumbCollection',
        'views/BreadcrumbsView',
        'backbone-queryparams'
], function (Backbone, 
		     GraphView, 
             RouteTableView, 
             StepView, 
             Statistic, 
             StatisticCollection, 
             Statistics,
             Route, 
             RouteCollection, 
             Server, 
             TimeUnit, 
             BreadcrumbCollection, 
             BreadcrumbsView) {
	var AppRouter = Backbone.Router.extend({

    	routes: {
            '':'appRoutes',
            'console/app':'appRoutes',
            'console/app/:from/:to':'appRoutes',
            'console/route/:id':'appRoute',
            'console/route/:id/:from/:to':'appRoute'
        },

        state: {
            routeId: null, // if empty or null, we're showing all the routes. Else, we're showing a specific route
            from: null,
            to: null,
            updateOverview: false,
            createRoute: function() {
            	if (this.routeId) {
            		if (this.from && this.to) {
            			return 'console/route/' + this.routeId + '/' + this.from + '/' + this.to;
            		}
            		else {
            			return 'console/route/' + this.routeId;
            		}
            	}
            	else {
            		if (this.from && this.to) {
                		return 'console/app/' + this.from + '/' + this.to;
            		}
            		else {
            			return '/console/app';
            		}
            	}
            },
            changeRoute: function(routeId) {
            	this.routeId = routeId;
            	this.updateOverview = true;
            },
            changeRange: function(from, to) {
            	this.from = from;
            	this.to = to;
            },
            toTimeUnit: function() {
            	if (this.from && this.to) {
            		return TimeUnit.forRange(this.from, this.to);
            	}
            	else {
            		return TimeUnit.day;
            	}
            },
            toApiRoute: function() {
            	if (this.routeId) {
            		return this.routeId;
            	}
            	else {
            		return 'overall';
            	}
            }
    	},
        
        initialize: function () {
        	this.breadcrumbCollection = new BreadcrumbCollection();
            this.breadcrumbsView = new BreadcrumbsView({collection: this.breadcrumbCollection, el: '#breadcrumbs'});
            this.breadcrumbsView.on("pathChanged", function(routeId) {
            	this.state.changeRoute(routeId);
            	Backbone.history.navigate(this.state.createRoute(), true);
            }, this);
            this.breadcrumbsView.render();
            
            this.statistics = new Statistics();
            
            this.graphView = new GraphView({model: this.statistics, el: '#stats'});
            this.graphView.on("rangeSelected", function(from, to) {
            	this.state.changeRange(from, to);
            	Backbone.history.navigate(this.state.createRoute(), true);
            }, this);
            this.graphView.render();
            this.updateOverview();
            
            this.routeCollection = new RouteCollection();
            this.routeTableView = new RouteTableView({collection: this.routeCollection, el: '#routes'});
            this.routeTableView.on("routeSelected", function(routeId) {
            	this.state.changeRoute(routeId);
            	Backbone.history.navigate(this.state.createRoute(), true);
            }, this);
            this.routeTableView.render();
            
            Backbone.history.start({pushState: true});
        },

        appRoutes: function(from, to) {
        	var updateOverview = this.state.updateOverview;
        	this.updateState(null, from, to);
        	if (updateOverview) {
        		this.updateOverview();
        	}
        	this.updateStatistics();
            var that = this;
            this.routeCollection.fetch({success: function() {
            	that.updateRoutes();
            }});
            this.breadcrumbCollection.home();
            // hack to remove the step view. TODO find a better way to do that
            $('#steps').empty();
        },

        appRoute: function(routeId, from, to) {
        	var updateOverview = this.state.updateOverview;
        	this.updateState(routeId, from, to);
        	if (updateOverview) {
        		this.updateOverview();
        	}
        	this.updateStatistics();
        	console.log("route detail", routeId);
        	// TODO: this is a hack to get the route with a given ID. It should be improved.
        	var routeCollection = new RouteCollection();
        	var that = this;
        	routeCollection.fetch({success: function(collection) {
                console.log("all routes", collection);
                var route = routeCollection.find(function(model) {
                    return model.get('routeId') == routeId;
                });
                var routes = [];
                _.each(Object.keys(route.get('steps')), function (step) {
                    routes.push(new Route({routeId:step, uri:route.get('steps')[step]}));
                });
                that.routeCollection.reset(routes);
                that.updateRoutes();
                new StepView({model:route, el:'#steps'}).render();
            }});
        	
            this.breadcrumbCollection.route(routeId);
        },
        updateState: function(routeId, from, to) {
        	this.state.routeId = routeId;
        	this.state.from = from;
        	this.state.to = to;
        	this.updateOverview = false;
        },
        updateStatistics: function() {
        	var that = this;
        	var options = {
        		from: that.state.from,
        		to: that.state.to,
        		callback: function(data) {
        			data = JSON.parse(data);
                    var stats = [];
                    data.forEach(function (elem) {
                        var statistic = new Statistic({range: elem[0], 
                        	                           failed: elem[1], 
                        	                           completed: elem[2],
                        	                           min: elem[3], 
                        	                           max: elem[4], 
                        	                           average: elem[5]});
                        stats.push(statistic);
                    });
                    that.statistics.get("statisticCollection").reset(stats);
        		}
        	};
        	var timeUnit = this.state.toTimeUnit();
        	this.statistics.set("timeUnit", timeUnit);
        	Server.statsPerElementAndTimeUnit(this.state.toApiRoute(), timeUnit.name, options);
        },
        updateOverview: function() {
        	var that = this;
        	var options = {
        		callback: function(data) {
        			data = JSON.parse(data);
                    var stats = [];
                    data.forEach(function (elem) {
                        var statistic = new Statistic({range: elem[0], 
                        	                           failed: elem[1], 
                        	                           completed: elem[2],
                        	                           min: elem[3], 
                        	                           max: elem[4], 
                        	                           average: elem[5]});
                        stats.push(statistic);
                    });
                    that.statistics.get("overviewCollection").reset(stats);
        		}
        	};
        	Server.statsPerElementAndTimeUnit(this.state.toApiRoute(), TimeUnit.day.name, options);
        },
        updateRoutes: function() {
        	var that = this;
        	this.routeCollection.each(function(route) {
        		var options = {
    	    		from: that.state.from,
    	    		to: that.state.to,
    	    		callback: function(data) {
    	    			var silent = {silent: true};
    	    			data = JSON.parse(data);
    		            // TODO this is a hack, and I wouldn't be surprised if it returned incorrect results: we should 
    	    			// have an API for getting the aggregated stats for a range directly
    		            data.forEach(function (elem) {
    		                route.set('failureCount', elem[1], silent);
    		                route.set('successCount', elem[2], silent);
    		                route.set('minimumTime', elem[3], silent);
    		                route.set('maximumTime', elem[4], silent);
    		                route.set('averageTime', elem[5], silent);
    		                route.set('messageCount', elem[1] + elem[2], silent);
    		            });
    		            var successRate = 0;
    		            if (route.get('messageCount') != 0) {
    		                successRate = Math.floor(route.get('successCount') * 100 / route.get('messageCount'));
    		            }
    		            route.set('successRate', successRate, silent);
    		            route.change();
    	    		}
            	};
        		Server.statsPerElementAndTimeUnit(route.get("routeId"), 'year', options);
        	});
        }
    });

    return AppRouter;

});