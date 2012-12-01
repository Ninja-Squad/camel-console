function () {
    emit(this.elementId, {
        completed:this.completed,
        failed:this.failed,
        min:this.min,
        max:this.max,
        avg:this.average
    });
}
