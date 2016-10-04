function createChart() {
    var ctx = $("#chart");
    var ffi = ctx.data("ffi");
    var ffd = ctx.data("ffd");
    var pack9 = ctx.data("pack9");
    var wisedb = ctx.data("wisedb");

    var myChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ["ffd", "ffi", "pack9", "wiseDB"],
            datasets: [{
                label: '# of Votes',
                data: [ffi, ffd, pack9, wiseDB],
                backgroundColor: [
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(54, 162, 235, 0.2)',
                    'rgba(255, 206, 86, 0.2)',
                    'rgba(75, 192, 192, 0.2)',
                    'rgba(153, 102, 255, 0.2)',
                    'rgba(255, 159, 64, 0.2)'
                ],
                borderColor: [
                    'rgba(255,99,132,1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 159, 64, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero:true
                    }
                }]
            }
        }
    });

}