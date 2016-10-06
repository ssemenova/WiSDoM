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

    props: ["mode", "rlearnData"],

    methods: {
        haveData: function () {
            return this.rlearnData.length != 0;
        },
        
        redrawGraph: function () {
            const layout = {
                autosize: false,
                width: 400,
                height: 360,
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

            for (let v of this.rlearnData) {
                x.push(v.tick/100);
                y.push(v.cost);
            }
            
            Plotly.purge("rlearnGraph");
            Plotly.newPlot("rlearnGraph",
                           [{x, y, type: "scatter", name: "Cost"}],
                           layout, {displayModeBar: false});
        }
    },

    watch: {
        rlearnData: function (newData) {
            this.redrawGraph();
        }
    }
};

