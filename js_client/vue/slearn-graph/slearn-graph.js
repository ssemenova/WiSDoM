const axios = require("axios");
const Plotly = require("plotly.js/lib/core");
Plotly.register([
    require('plotly.js/lib/bar')
]);

module.exports = {
    data: function () {
        return {
            costs: {},
            waiting: true,
            requestedCloud: false,
            waitingOnCloud: true,
            cloudCost: 0
        };
    },

    props: ["mode", "sla"],

    methods: {
        redrawGraph: function () {
            const layout = {
                autosize: false,
                width: 360,
                height: 360,
                margin: {
                    b: 70,
                    l: 40,
                    t: 70,
                    r: 40
                },
                yaxis: {
                    showgrid: true,
                    gridcolor: "#bdbdbd",
                    title: "Cost (cents)"
                },
                title: "Performance v. Heuristics"
            };

            const x = ["FFD", "FFI", "Pack9", "WiSeDB"];
            const y = [this.costs.ffd, this.costs.ffi, this.costs.pack9, this.costs.wisedb];

            if ((!this.waitingOnCloud) && this.requestedCloud) {
                x.push("Actual cost");
                y.push(this.cloudCost);
            }
            
            Plotly.purge("slearnGraph");
            Plotly.newPlot("slearnGraph",
                           [{x, y, type: "bar", name: "Cost"}],
                           layout, {displayModeBar: false});
        },

        sendToCloud: function () {
            this.requestedCloud = true;
            this.waitingOnCloud = true;
            axios.post("/cloudrun", this.sla)
                .then((res) => {
                    this.cloudCost = res.data.actualCost;
                    this.waitingOnCloud = false;
                    this.redrawGraph();
                }).catch((err) => console.log);
        }
        
    },

    watch: {
        sla: function (newSLA) {
            this.waiting = true;
            this.waitingOnCloud = true;
            this.requestedCloud = false;
            axios.post("/heuristics", newSLA)
                .then((res) => {
                    this.costs = res.data;
                    this.redrawGraph();
                    this.waiting = false;
                });

           
        }
    }
};
