const axios = require("axios");
const Plotly = require("plotly.js/lib/core");
Plotly.register([
    require('plotly.js/lib/bar')
]);

module.exports = {
    data: function () {
        return {
            
        };
    },

    props: ["mode", "rlearnData", "stats"],

    methods: {
        haveData: function () {
            return this.rlearnData.length != 0;
        },
        
        redrawGraph: function () {
            const layout = {
                autosize: false,
                width: 450,
                height: 400,
                margin: {
                    b: 80,
                    l: 80,
                    t: 70,
                    r: 40
                },
                yaxis: {
                    showgrid: true,
                    title: "Cost (1/100 cent)"
                },

                xaxis: {
                    title: "Time (s)"
                },
                
                title: "Average Cost Per Query"
            };

            const x = [];
            const y = [];

            const x2 = [];
            const y2 = [];

            for (let v of this.rlearnData) {
                if (!v.clairvoyant) {
                    x.push(v.tick/100);
                    y.push(v.cost);
                } else {
                    x2.push(v.tick/100);
                    y2.push(v.cost);
                }
            }
            
            Plotly.purge("rlearnGraph");
            Plotly.newPlot("rlearnGraph",
                           [{x, y, type: "scatter", name: "RLearn"},
                            {x: x2, y: y2, type: "scatter", name: "Clairvoyant"}],
                           layout, {displayModeBar: false});
        },

        redrawPenaltyGraph: function () {
            const layout = {
                autosize: false,
                width: 450,
                height: 400,
                margin: {
                    b: 80,
                    l: 80,
                    t: 70,
                    r: 40
                },
                yaxis: {
                    showgrid: true,
                    title: "Cost (1/100 cent)"
                },

                xaxis: {
                    title: "Time (s)"
                },
                
                title: "Average Penalty Per Query"
            };

            const x = [];
            const y = [];

            const x2 = [];
            const y2 = [];

            for (let v of this.rlearnData) {
                if (!v.clairvoyant) {
                    x.push(v.tick/100);
                    y.push(v.penalty);
                } else {
                    x2.push(v.tick/100);
                    y2.push(v.penalty);
                }
            }
            
            Plotly.purge("rlearnGraphPenalty");
            Plotly.newPlot("rlearnGraphPenalty",
                           [{x, y, type: "scatter", name: "RLearn"},
                            {x: x2, y: y2, type: "scatter", name: "Clairvoyant"}],
                           layout, {displayModeBar: false});
        }
    },

    watch: {
        rlearnData: function (newData) {
            this.redrawGraph();
            this.redrawPenaltyGraph();
        }
    }
};

