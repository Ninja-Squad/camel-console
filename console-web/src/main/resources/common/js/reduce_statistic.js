// for the stats of an elementId (key), will compute every field to build an aggregated stat
function (key, values) {
    var completed = 0;
    var failed = 0;
    var min = values[0].min;
    var max = 0;
    var avg = 0;
    values.forEach(function (value) {
        avg = (avg*completed + value.completed*value.avg)/(completed + value.completed);
        completed += value.completed;
        failed += value.failed;
        if(value.min < min) min = value.min;
        if(value.max > max) max = value.max;
    });
    return {
        completed:completed,
        failed:failed,
        min:min,
        max:max,
        average:avg
    };
}
