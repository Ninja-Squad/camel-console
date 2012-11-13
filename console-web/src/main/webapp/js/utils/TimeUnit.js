define([], function () {
    /**
     * The time units handled by the API.
     * Each TimeUnit contains a time format to use in the graph view, a number of milliseconds 
     * for this time unit (used to compute the width of the graph view bars), a minimum range,
     * used to decide the most appropriate time unit for a given range, and a name, used to pass 
     * an argument to the URL.
     */
    var TimeUnit = {
        month: {
            timeFormat: '%b %y',
            millis: 28 * 24 * 60 * 60 * 1000,
            minRange: 104 * 7 * 24 * 60 * 60 * 1000,
            name: 'month'
        },
        week: {
            timeFormat: 'week of %y/%m/%0d',
            millis: 7 * 24 * 60 * 60 * 1000,
            minRange: 120 * 24 * 60 * 60 * 1000,
            name: 'week'
        },
        day: {
            timeFormat: '%y/%m/%0d',
            millis: 24 * 60 * 60 * 1000,
            minRange: 120 * 60 * 60 * 1000,
            name: 'day'
        },
        hour: {
            timeFormat: '%y/%m/%0d %H:%M',
            millis: 60 * 60 * 1000,
            minRange: 120 * 60 * 1000,
            name: 'hour'
        },
        minute: {
            timeFormat: '%y/%m/%0d %H:%M',
            millis: 60 * 1000,
            minRange: 120 * 1000,
            name: 'minute'
        },
        second: {
            timeFormat: '%y/%m/%0d %H:%M:%S',
            millis: 1000,
            minRange: 0,
            name: 'second'
        },
        
        /**
         * Returns the appropriate TimeUnit for the given range
         */
        forRange: function(fromMillis, toMillis) {
            var range = toMillis - fromMillis;
            var units = [minute, hour, day, week, month];
            var result;
            for (var i = 0; i < units.length; i++) {
                if (range >= units[i].minRange) {
                    result = units[i];
                }
                else {
                    break;
                }
            }
            return result;
        }
    }
    
    return TimeUnit;
});
