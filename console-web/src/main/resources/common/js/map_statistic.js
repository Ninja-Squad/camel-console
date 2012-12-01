// for each stat will emit an object containing each field interesting, with this elementId as a key
function () {
    emit(this.elementId, {
        completed:this.completed,
        failed:this.failed,
        min:this.min,
        max:this.max,
        avg:this.average
    });
}
