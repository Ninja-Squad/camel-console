function (k, v) {
    var count = 0;
    v.forEach(function (value) {
        count += value.count
    });
    return count;
}